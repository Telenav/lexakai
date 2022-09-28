////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.lexakai.dependencies;

import com.telenav.kivakit.component.BaseComponent;
import com.telenav.kivakit.core.language.primitive.Ints;
import com.telenav.kivakit.core.os.OperatingSystem;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.filesystem.Folder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;
import static com.telenav.kivakit.resource.FileName.parseFileName;

/**
 * @author jonathanl (shibo)
 */
public class MavenDependencyTreeBuilder extends BaseComponent
{
    private final Folder root;

    public MavenDependencyTreeBuilder(Folder root)
    {
        this.root = root;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public Set<DependencyTree> trees()
    {
        var mavenHome = ensureNotNull(OperatingSystem.operatingSystem().systemPropertyOrEnvironmentVariable("M2_HOME"), "Must set M2_HOME to point to maven installation");
        var output = OperatingSystem.operatingSystem()
                .execute(this, root.asJavaFile(), mavenHome + "/bin/mvn", "-DoutputType=tgf", "dependency:tree")
                .replaceAll("\\[INFO]", "");

        var matcher = Pattern.compile("--- maven-dependency-plugin.*?@ (?<projectArtifactId>.*?) ---" +
                        "(?<dependencies>.*?)#" +
                        "(?<references>.*?)---",
                Pattern.DOTALL).matcher(output);

        var artifactIdToFolder = new HashMap<String, Folder>();
        root.nestedFiles(file -> file.fileName().equals(parseFileName(this, "pom.xml")))
                .forEach(file ->
                {
                    var pom = file.reader().asString().replaceAll("(?s)<parent>.*</parent>", "");
                    var artifactId = Strings.extractFirstGroup(pom, "(?s)<artifactId>(.*?)</artifactId>");
                    artifactIdToFolder.put(artifactId, file.parent());
                });

        var trees = new HashSet<DependencyTree>();

        while (matcher.find())
        {
            var projectArtifactId = matcher.group("projectArtifactId").trim();
            var dependencies = matcher.group("dependencies");
            var references = matcher.group("references");
            var projectFolder = artifactIdToFolder.get(projectArtifactId);

            var tree = new DependencyTree(projectArtifactId, projectFolder);

            var dependencyPattern = Pattern.compile("^(\\d+) (.*?):(.*?):jar:(.*?)(:.*$)?");
            var dependencyMatcher = dependencyPattern.matcher(dependencies);
            while (dependencyMatcher.find())
            {
                var identifier = Ints.parseInt(this, dependencyMatcher.group(1));
                var groupId = dependencyMatcher.group(2);
                var artifactId = dependencyMatcher.group(3);
                var version = dependencyMatcher.group(4);
                var artifact = new Artifact(identifier, groupId, artifactId, version);
                tree.add(artifact);
            }

            var referencePattern = Pattern.compile("^(\\d+) (\\d+) (.*?)$");
            var referenceMatcher = referencePattern.matcher(references);
            while (referenceMatcher.find())
            {
                var fromIdentifier = Ints.parseInt(this, referenceMatcher.group(1));
                var toIdentifier = Ints.parseInt(this, referenceMatcher.group(2));
                var from = tree.artifact(fromIdentifier);
                var to = tree.artifact(toIdentifier);
                if (from != null && to != null)
                {
                    tree.add(new Dependency(from, to));
                }
                else
                {
                    warning("Could not find artifacts ${long} and ${long}", fromIdentifier, toIdentifier);
                }
            }

            if (!tree.isEmpty())
            {
                trees.add(tree);
            }
        }

        return trees;
    }
}
