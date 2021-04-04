package com.telenav.lexakai.library;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for extracting information from {@link AnnotationExpr} elements.
 *
 * @author jonathanl (shibo)
 */
public class Annotations
{
    /**
     * @return A set of all the annotations of the given type
     */
    public static Set<AnnotationExpr> annotationsOfType(final NodeWithAnnotations<?> type,
                                                        final Class<? extends Annotation> annotationType)
    {
        final var annotations = new HashSet<AnnotationExpr>();
        for (final var annotation : type.getAnnotations())
        {
            if (annotation.getName().asString().equals(annotationType.getSimpleName()))
            {
                annotations.add(annotation);
            }
        }
        return annotations;
    }

    /**
     * @return The boolean value for the given annotation member
     */
    public static boolean booleanValue(final AnnotationExpr annotation, final String member, final boolean defaultValue)
    {
        final var expression = expression(annotation, member);
        if (expression instanceof BooleanLiteralExpr)
        {
            return expression.asBooleanLiteralExpr().getValue();
        }
        return defaultValue;
    }

    public static String className(final AnnotationExpr annotation, final String member)
    {
        final var expression = expression(annotation, member);
        if (expression != null && expression.isClassExpr())
        {
            return Name.of(expression.asClassExpr(), Name.Qualification.QUALIFIED, Name.TypeParameters.WITHOUT_TYPE_PARAMETERS);
        }
        return null;
    }

    /**
     * @return The set of class names for the given annotation member
     */
    public static Set<String> classNames(final AnnotationExpr annotation, final String member)
    {
        final var expression = expression(annotation, member);
        if (expression instanceof ArrayInitializerExpr)
        {
            final var array = expression.asArrayInitializerExpr();
            final var classes = new HashSet<String>();
            for (final var value : array.getValues())
            {
                classes.add(Name.of(value.asClassExpr(), Name.Qualification.QUALIFIED, Name.TypeParameters.WITHOUT_TYPE_PARAMETERS));
            }
            return classes;
        }
        if (expression instanceof SingleMemberAnnotationExpr)
        {
            final var qualifiedName = Name.of(expression.asClassExpr(), Name.Qualification.QUALIFIED, Name.TypeParameters.WITHOUT_TYPE_PARAMETERS);
            return qualifiedName == null ? Set.of() : Set.of(qualifiedName);
        }
        return Set.of();
    }

    /**
     * @return The set of class names for the given annotation
     */
    public static Set<String> classNames(final AnnotationExpr annotation)
    {
        if (annotation instanceof MarkerAnnotationExpr)
        {
            return Set.of();
        }

        final var expression = annotation.asSingleMemberAnnotationExpr().getMemberValue();
        if (expression instanceof ArrayInitializerExpr)
        {
            final var array = expression.asArrayInitializerExpr();
            final var classes = new HashSet<String>();
            for (final var value : array.getValues())
            {
                final var name = Name.of(value.asClassExpr(), Name.Qualification.UNQUALIFIED, Name.TypeParameters.WITHOUT_TYPE_PARAMETERS);
                if (name != null)
                {
                    classes.add(name);
                }
            }
            return classes;
        }

        if (expression instanceof ClassExpr)
        {
            final var qualifiedName = Name.of(expression.asClassExpr(), Name.Qualification.UNQUALIFIED, Name.TypeParameters.WITHOUT_TYPE_PARAMETERS);
            return qualifiedName == null ? Set.of() : Set.of(qualifiedName);
        }

        return Set.of();
    }

    /**
     * @return The expression for the designated annotation member
     */
    public static Expression expression(final AnnotationExpr annotation, final String member)
    {
        if (annotation instanceof MarkerAnnotationExpr)
        {
            return null;
        }
        for (final var pair : annotation.asNormalAnnotationExpr().getPairs())
        {
            if (pair.getName().asString().equals(member))
            {
                final var value = pair.getValue();
                if (value != null)
                {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * @return The string value of the given annotation member
     */
    public static String stringValue(final AnnotationExpr annotation, final String member)
    {
        final var expression = expression(annotation, member);
        if (expression instanceof StringLiteralExpr)
        {
            return expression.asStringLiteralExpr().asString();
        }
        if (expression instanceof NameExpr)
        {
            final var type = expression.asNameExpr();
            return Name.simpleName(type);
        }
        if (expression instanceof ClassExpr)
        {
            return Name.of(expression.asClassExpr(), Name.Qualification.QUALIFIED, Name.TypeParameters.WITHOUT_TYPE_PARAMETERS);
        }
        return null;
    }

    /**
     * @return The single string value for the given annotation expression
     */
    public static String stringValue(final AnnotationExpr annotation)
    {
        final var expression = annotation.asSingleMemberAnnotationExpr();
        final var value = expression.getMemberValue();
        if (value != null)
        {
            return value.asStringLiteralExpr().asString();
        }
        return null;
    }

    /**
     * @return The set of string values for the given annotation expression
     */
    @NotNull
    public static Set<String> stringValues(final AnnotationExpr annotation, final String member)
    {
        final var expression = expression(annotation, member);
        if (expression instanceof ArrayInitializerExpr)
        {
            final var array = expression.asArrayInitializerExpr();
            final var annotations = new HashSet<String>();
            for (final var value : array.getValues())
            {
                annotations.add(value.asStringLiteralExpr().asString());
            }
            return annotations;
        }
        if (expression instanceof StringLiteralExpr)
        {
            return Set.of(expression.asStringLiteralExpr().asString());
        }
        return Set.of();
    }

    /**
     * @return The value of the given member of the given normal annotation
     */
    public static Expression value(final AnnotationExpr annotation, final String memberName)
    {
        for (final var pair : annotation.asNormalAnnotationExpr().getPairs())
        {
            if (Name.simpleName(pair).equals(memberName))
            {
                return pair.getValue();
            }
        }
        return null;
    }
}
