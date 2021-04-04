package com.telenav.lexakai.builders;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.telenav.kivakit.core.kernel.language.strings.formatting.IndentingStringBuilder;
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

    public MethodBuilder(final LexakaiClassDiagram diagram, final TypeDeclaration<?> type)
    {
        this.diagram = diagram;
        this.type = type;
    }

    /**
     * Adds any method declarations this type has to the given builder
     */
    public void addMethodDeclarations(final IndentingStringBuilder builder)
    {
        // If this type should include members,
        if (diagram.includeMembers(type))
        {
            // get method groups,
            final var groups = new MethodGrouper(new UmlType(diagram, type)).groups();

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
            final var none = groups.none();
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
