package com.telenav.lexakai.types;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.telenav.kivakit.core.kernel.language.strings.formatting.IndentingStringBuilder;
import com.telenav.lexakai.LexakaiClassDiagram;
import com.telenav.lexakai.builders.MethodBuilder;
import com.telenav.lexakai.builders.TypeBuilder;
import com.telenav.lexakai.library.Annotations;
import com.telenav.lexakai.library.Name;
import com.telenav.lexakai.library.Name.Qualification;
import com.telenav.lexakai.library.Name.TypeParameters;
import com.telenav.lexakai.members.UmlMethod;
import com.telenav.lexakai.annotations.UmlClassDiagram;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.telenav.lexakai.library.Name.Qualification.QUALIFIED;
import static com.telenav.lexakai.library.Name.Qualification.UNQUALIFIED;
import static com.telenav.lexakai.library.Name.TypeParameters.WITHOUT_TYPE_PARAMETERS;
import static com.telenav.lexakai.library.Name.TypeParameters.WITH_TYPE_PARAMETERS;

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
    public UmlType(final LexakaiClassDiagram diagram, final TypeDeclaration<?> type)
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
        final var expression = type.getAnnotationByClass(UmlClassDiagram.class);
        if (expression.isPresent())
        {
            return Annotations.stringValues(expression.get(), "documentationSections");
        }
        return Set.of();
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof UmlType)
        {
            final UmlType that = (UmlType) object;
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
        final var methods = new ArrayList<UmlMethod>();
        diagram.includedMethods(type, at ->
        {
            // if the method is not excluded,
            final var method = new UmlMethod(type.asClassOrInterfaceDeclaration(), at);
            if (!method.isExcluded())
            {
                methods.add(method);
            }
        });
        return methods;
    }

    public String name(final Qualification qualification, final TypeParameters parameters)
    {
        return Name.of(type, qualification, parameters);
    }

    public String simpleName()
    {
        return Name.simpleName(type);
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
     * @return The UML for this type
     */
    public String uml()
    {
        if (uml == null)
        {
            // Create a string builder,
            final var builder = IndentingStringBuilder.defaultTextIndenter();

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
                builder.appendLine("enum " + Name.of(type, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS));

                // add open curly,
                builder.appendLine("{");
                builder.indent();

                // add enum entries,
                final var sorted = new ArrayList<>(type.asEnumDeclaration().getEntries());
                sorted.sort(Comparator.comparing(value -> value.getName().asString()));
                for (final var entry : sorted)
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
                builder.appendLine("annotation " + Name.of(type, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS));
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
