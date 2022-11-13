////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2022 Telenav, Inc.
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

package com.telenav.lexakai.types;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.telenav.kivakit.core.string.IndentingStringBuilder;
import com.telenav.lexakai.LexakaiClassDiagram;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.builders.MethodBuilder;
import com.telenav.lexakai.builders.TypeBuilder;
import com.telenav.lexakai.library.Annotations;
import com.telenav.lexakai.library.Names;
import com.telenav.lexakai.library.Names.Qualification;
import com.telenav.lexakai.library.Names.TypeParameters;
import com.telenav.lexakai.members.UmlMethod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.telenav.lexakai.library.Names.Qualification.QUALIFIED;
import static com.telenav.lexakai.library.Names.Qualification.UNQUALIFIED;
import static com.telenav.lexakai.library.Names.TypeParameters.WITHOUT_TYPE_PARAMETERS;
import static com.telenav.lexakai.library.Names.TypeParameters.WITH_TYPE_PARAMETERS;

/**
 * A UML type declaration belonging to a particular diagram. A type can belong to multiple type groups.
 *
 * @author jonathanl (shibo)
 */
public class UmlType
{
    /** The particular diagram that this type belongs to */
    private final LexakaiClassDiagram diagram;

    /** The type if this is a class or interface */
    private final TypeDeclaration<?> type;

    /** The UML for this type */
    private String uml;

    /**
     * Constructor for classes an interfaces
     */
    public UmlType(LexakaiClassDiagram diagram, TypeDeclaration<?> type)
    {
        this.diagram = diagram;
        this.type = type;
    }

    public LexakaiClassDiagram diagram()
    {
        return diagram;
    }

    public Set<String> documentationSections()
    {
        var expression = type.getAnnotationByClass(UmlClassDiagram.class);
        return expression.map(annotation -> Annotations.stringValues(annotation, "documentationSections")).orElseGet(Set::of);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof UmlType that)
        {
            return name(UNQUALIFIED, WITHOUT_TYPE_PARAMETERS).equals(that.name(UNQUALIFIED, WITHOUT_TYPE_PARAMETERS));
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name(UNQUALIFIED, WITHOUT_TYPE_PARAMETERS));
    }

    public List<UmlMethod> includedMethods()
    {
        // For each included method,
        var methods = new ArrayList<UmlMethod>();
        diagram.includedMethods(type, at ->
        {
            // if the method is not excluded,
            var method = new UmlMethod(type.asClassOrInterfaceDeclaration(), at);
            if (!method.isExcluded())
            {
                methods.add(method);
            }
        });
        return methods;
    }

    public String name(Qualification qualification, TypeParameters parameters)
    {
        return Names.name(type, qualification, parameters);
    }

    public String simpleName()
    {
        return Names.simpleName(type);
    }

    @Override
    public String toString()
    {
        return name(QUALIFIED, WITH_TYPE_PARAMETERS);
    }

    public TypeDeclaration<?> type()
    {
        return type;
    }

    /**
     * Returns the UML for this type
     */
    public String uml()
    {
        if (uml == null && !Annotations.shouldExcludeType(type))
        {
            // Create a string builder,
            var builder = IndentingStringBuilder.defaultTextIndenter();

            if (type.isClassOrInterfaceDeclaration())
            {
                // add the type declaration,
                new TypeBuilder(diagram, type).addTypeDeclaration(builder);

                // add open curly,
                builder.appendLine("{");
                builder.indent();

                // add method declarations,
                new MethodBuilder(diagram, type).addMethodDeclarations(builder);

                // add close curly
                builder.unindent();
                builder.appendLine("}");
                builder.appendLine("");
            }

            if (type.isEnumDeclaration())
            {
                // add the enum declaration,
                builder.appendLine("enum " + Names.name(type, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS));

                // add open curly,
                builder.appendLine("{");
                builder.indent();

                // add enum entries,
                var sorted = new ArrayList<>(type.asEnumDeclaration().getEntries());
                sorted.sort(Comparator.comparing(value -> value.getName().asString()));
                for (var entry : sorted)
                {
                    builder.appendLine(entry.getName().asString());
                }

                // add close curly,
                builder.unindent();
                builder.appendLine("}");
                builder.appendLine("");
            }

            if (type.isAnnotationDeclaration())
            {
                // add the annotation declaration,
                builder.appendLine("annotation " + Names.name(type, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS));
                if (!diagram.includedMethods(type).isEmpty())
                {
                    // add open curly,
                    builder.appendLine("{");
                    builder.indent();

                    // add method declarations,
                    new MethodBuilder(diagram, type).addMethodDeclarations(builder);

                    // add close curly,
                    builder.unindent();
                    builder.appendLine("}");
                }
                builder.appendLine("");
            }

            // and form UML string.
            uml = builder.toString();
        }

        return uml;
    }
}
