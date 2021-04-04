package com.telenav.lexakai.dependencies;

import com.telenav.kivakit.core.filesystem.Folder;
import com.telenav.kivakit.core.kernel.language.primitives.Ints;
import com.telenav.kivakit.core.kernel.language.strings.Strings;
import com.telenav.kivakit.core.kernel.language.vm.OperatingSystem;
import com.telenav.kivakit.core.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.resource.path.FileName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author jonathanl (shibo)
 */
public class MavenDependencyTreeBuilder extends BaseRepeater
{
    private final Folder root;

    public MavenDependencyTreeBuilder(final Folder root)
    {
        this.root = root;
    }

    public Set<DependencyTree> trees()
    {
        final var output = OperatingSystem.get()
                .exec(root.asJavaFile(), "mvn", "-DoutputType=tgf", "dependency:tree")
                .replaceAll("\\[INFO]", "");

        final var matcher = Pattern.compile("--- maven-dependency-plugin.*?@ (?<projectArtifactId>.*?) ---" +
                        "(?<dependencies>.*?)#" +
                        "(?<references>.*?)---",
                Pattern.DOTALL).matcher(output);

        final var artifactIdToFolder = new HashMap<String, Folder>();
        root.nestedFiles(file -> new FileName("pom.xml").fileMatcher().matches(file))
                .forEach(file ->
                {
                    final var pom = file.reader().string().replaceAll("(?s)<parent>.*</parent>", "");
                    final var artifactId = Strings.extract(pom, "(?s)<artifactId>(.*?)</artifactId>");
                    artifactIdToFolder.put(artifactId, file.parent());
                });

        final var trees = new HashSet<DependencyTree>();

        while (matcher.find())
        {
            final var projectArtifactId = matcher.group("projectArtifactId").trim();
            final var dependencies = matcher.group("dependencies");
            final var references = matcher.group("references");
            final var projectFolder = artifactIdToFolder.get(projectArtifactId);

            final var tree = new DependencyTree(projectArtifactId, projectFolder);

            final var dependencyPattern = Pattern.compile("(\\d+) (.*?):(.*?):jar:(.*?)(:.*)?");
            final var dependencyMatcher = dependencyPattern.matcher(dependencies);
            while (dependencyMatcher.find())
            {
                final var identifier = Ints.parse(dependencyMatcher.group(1));
                final var groupId = dependencyMatcher.group(2);
                final var artifactId = dependencyMatcher.group(3);
                final var version = dependencyMatcher.group(4);
                final var artifact = new Artifact(identifier, groupId, artifactId, version);
                tree.add(artifact);
            }

            final var referencePattern = Pattern.compile("(\\d+) (\\d+) (.*?)");
            final var referenceMatcher = referencePattern.matcher(references);
            while (referenceMatcher.find())
            {
                final var fromIdentifier = Ints.parse(referenceMatcher.group(1));
                final var toIdentifier = Ints.parse(referenceMatcher.group(2));
                final var from = tree.artifact(fromIdentifier);
                final var to = tree.artifact(toIdentifier);
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
