package com.telenav.lexakai.application.library;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.telenav.lexakai.annotations.UmlClassDiagram;

import java.util.HashSet;
import java.util.Set;

/**
 * @author jonathanl (shibo)
 */
public class Diagrams
{
    /**
     * @return The @UmlClassDiagram annotation with the given lowercase hyphenated diagram name
     */
    public static AnnotationExpr diagramAnnotation(final TypeDeclaration<?> type, final String diagramName)
    {
        for (final var annotation : Annotations.annotationsOfType(type, UmlClassDiagram.class))
        {
            final var expression = Annotations.value(annotation, "diagram");
            if (expression != null)
            {
                if (diagramName.equals(Name.of(expression.asClassExpr(), Name.Qualification.UNQUALIFIED, Name.TypeParameters.WITHOUT_TYPE_PARAMETERS)))
                {
                    return annotation;
                }
            }
        }
        return null;
    }

    /**
     * @return The set of diagrams that the given type belongs to
     */
    public static Set<String> diagrams(final TypeDeclaration<?> type, final boolean includePackageDiagram)
    {
        final var diagrams = new HashSet<String>();
        for (final var annotation : Annotations.annotationsOfType(type, UmlClassDiagram.class))
        {
            final var expression = Annotations.value(annotation, "diagram");
            if (expression != null)
            {
                final var name = Name.of(expression.asClassExpr(), Name.Qualification.UNQUALIFIED, Name.TypeParameters.WITHOUT_TYPE_PARAMETERS);
                if (name != null)
                {
                    diagrams.add(name);
                }
            }
        }

        if (includePackageDiagram)
        {
            final var qualifiedName = type.getFullyQualifiedName();
            if (qualifiedName.isPresent())
            {
                diagrams.add(Name.packageName(qualifiedName.get()));
            }
        }
        return diagrams;
    }
}
