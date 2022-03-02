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

package com.telenav.lexakai.builders;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.telenav.kivakit.core.string.IndentingStringBuilder;
import com.telenav.lexakai.LexakaiClassDiagram;
import com.telenav.lexakai.builders.grouper.MethodGrouper;
import com.telenav.lexakai.types.UmlType;

/**
 * @author jonathanl (shibo)
 */
public class MethodBuilder
{
    private final LexakaiClassDiagram diagram;

    private final TypeDeclaration<?> type;

    public MethodBuilder(LexakaiClassDiagram diagram, TypeDeclaration<?> type)
    {
        this.diagram = diagram;
        this.type = type;
    }

    /**
     * Adds any method declarations this type has to the given builder
     */
    public void addMethodDeclarations(IndentingStringBuilder builder)
    {
        // If this type should include members,
        if (diagram.includeMembers(type))
        {
            // get method groups,
            var groups = new MethodGrouper(new UmlType(diagram, type)).groups();

            // then add the UML for static method, constructors and ordinary methods.
            if (!groups.staticMethods().isEmpty())
            {
                builder.appendLines(groups.staticMethods().uml());
            }
            if (!groups.constructors().isEmpty())
            {
                builder.appendLine("--(constructors)--");
                groups.constructors().forEach(at -> builder.appendLine(at.uml()));
            }
            var none = groups.none();
            if (!none.isEmpty())
            {
                builder.appendLines(none.uml());
            }
            if (!groups.isEmpty())
            {
                groups.namedGroups().forEach(at -> builder.appendLines(at.uml()));
            }
        }
    }
}
