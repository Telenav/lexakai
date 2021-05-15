////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.lexakai.dependencies;

import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.language.collections.Collections;
import com.telenav.kivakit.kernel.language.strings.formatting.IndentingStringBuilder;
import com.telenav.lexakai.BaseLexakaiDiagram;

/**
 * @author jonathanl (shibo)
 */
public class DependencyDiagram extends BaseLexakaiDiagram
{
    private final Folder root;

    private final Folder outputRoot;

    private final DependencyTree tree;

    public DependencyDiagram(final Folder root, final Folder outputRoot, final DependencyTree tree)
    {
        this.root = root;
        this.outputRoot = outputRoot;
        this.tree = tree;
    }

    public File save()
    {
        final var relativeFolder = tree.projectFolder().relativeTo(root);
        final var outputFolder = outputRoot.folder(relativeFolder);

        final var file = outputFolder
                .folder("documentation/diagrams")
                .file("dependencies.puml");

        file.parent().mkdirs();
        file.writer().save(uml(tree.title()));

        return file;
    }

    @Override
    protected void onUml(final IndentingStringBuilder builder)
    {
        for (final var artifact : Collections.sorted(tree.artifacts()))
        {
            builder.appendLine("artifact " + artifact.artifactId());
        }

        for (final var dependency : Collections.sorted(tree.dependencies()))
        {
            builder.appendLine(dependency.uml());
        }

        builder.appendLine("");
    }
}
