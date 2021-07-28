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

package com.telenav.lexakai.library;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.telenav.lexakai.annotations.visibility.UmlExcludeType;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import static com.telenav.lexakai.library.Names.Qualification.UNQUALIFIED;
import static com.telenav.lexakai.library.Names.TypeParameters.WITHOUT_TYPE_PARAMETERS;

/**
 * Utility methods for extracting information from {@link AnnotationExpr} elements, including class names, expressions,
 * and string and boolean values.
 *
 * @author jonathanl (shibo)
 */
public class Annotations
{
    /**
     * @return A set of all the annotations of the given type
     */
    public static Set<AnnotationExpr> annotations(final NodeWithAnnotations<?> type,
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
            return Names.name(expression.asClassExpr(), Names.Qualification.QUALIFIED, Names.TypeParameters.WITHOUT_TYPE_PARAMETERS);
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
                classes.add(Names.name(value.asClassExpr(), Names.Qualification.QUALIFIED, Names.TypeParameters.WITHOUT_TYPE_PARAMETERS));
            }
            return classes;
        }
        if (expression instanceof SingleMemberAnnotationExpr)
        {
            final var qualifiedName = Names.name(expression.asClassExpr(), Names.Qualification.QUALIFIED, Names.TypeParameters.WITHOUT_TYPE_PARAMETERS);
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
                final var name = Names.name(value.asClassExpr(), Names.Qualification.UNQUALIFIED, Names.TypeParameters.WITHOUT_TYPE_PARAMETERS);
                if (name != null)
                {
                    classes.add(name);
                }
            }
            return classes;
        }

        if (expression instanceof ClassExpr)
        {
            final var qualifiedName = Names.name(expression.asClassExpr(), Names.Qualification.UNQUALIFIED, Names.TypeParameters.WITHOUT_TYPE_PARAMETERS);
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

    public static boolean hasAnnotation(final NodeWithAnnotations<?> type,
                                        final Class<? extends Annotation> annotationType)
    {
        return !annotations(type, annotationType).isEmpty();
    }

    public static boolean shouldExcludeType(final TypeDeclaration<?> type)
    {
        var marker = false;
        for (final var annotation : Annotations.annotations(type, UmlExcludeType.class))
        {
            final var expression = Annotations.value(annotation, "value");
            if (expression != null)
            {
                final var thatTypeName = Names.name(expression.asClassExpr(), UNQUALIFIED, WITHOUT_TYPE_PARAMETERS);
                final var thisTypeName = Names.name(type, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS);
                if (thisTypeName.equals(thatTypeName))
                {
                    return true;
                }
            }
            else
            {
                marker = true;
            }
        }
        return marker;
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
            return Names.simpleName(type);
        }
        if (expression instanceof ClassExpr)
        {
            return Names.name(expression.asClassExpr(), Names.Qualification.QUALIFIED, Names.TypeParameters.WITHOUT_TYPE_PARAMETERS);
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
        if (annotation.isMarkerAnnotationExpr())
        {
            return null;
        }

        if (annotation.isSingleMemberAnnotationExpr())
        {
            return annotation.asSingleMemberAnnotationExpr().getMemberValue();
        }

        for (final var pair : annotation.asNormalAnnotationExpr().getPairs())
        {
            if (Names.simpleName(pair).equals(memberName))
            {
                return pair.getValue();
            }
        }

        return null;
    }
}
