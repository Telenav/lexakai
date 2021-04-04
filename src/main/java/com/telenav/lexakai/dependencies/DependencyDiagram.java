package com.telenav.lexakai.dependencies;

import com.telenav.kivakit.core.filesystem.File;
import com.telenav.kivakit.core.kernel.language.collections.Collections;
import com.telenav.kivakit.core.kernel.language.strings.formatting.IndentingStringBuilder;
import com.telenav.lexakai.BaseLexakaiDiagram;

/**
 * @author jonathanl (shibo)
 */
public class DependencyDiagram extends BaseLexakaiDiagram
{
    private final DependencyTree tree;

    public DependencyDiagram(final DependencyTree tree)
    {
        this.tree = tree;
    }

    public File save()
    {
        final var file = tree.projectFolder()
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
