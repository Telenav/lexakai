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
        builder.appendLine("!include ../lexakai/lexakai.theme");
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
