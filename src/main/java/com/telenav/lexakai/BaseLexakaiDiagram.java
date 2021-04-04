package com.telenav.lexakai;

import com.telenav.kivakit.core.kernel.language.strings.formatting.IndentingStringBuilder;
import com.telenav.kivakit.core.kernel.messaging.repeaters.BaseRepeater;

/**
 * @author jonathanl (shibo)
 */
public abstract class BaseLexakaiDiagram extends BaseRepeater
{
    public String uml(final String title)
    {
        // Create a string builder,
        final var builder = IndentingStringBuilder.defaultTextIndenter();

        // add the PlantUML prologue,
        builder.appendLine("");
        builder.appendLine("@startuml");
        builder.appendLine("");
        builder.indent();
        builder.appendLine("!include ../lexakai.theme");
        builder.appendLine("");
        builder.appendLine("title \"" + title + "\"");
        builder.appendLine("");

        onUml(builder);

        // then add the epilogue,
        builder.unindent();
        builder.appendLine("@enduml");
        builder.appendLine("");

        // and return the uml.
        return builder.toString();
    }

    protected abstract void onUml(IndentingStringBuilder builder);
}
