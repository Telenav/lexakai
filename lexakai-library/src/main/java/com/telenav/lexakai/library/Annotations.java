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
     * Returns a set of all the annotations of the given type
     */
    public static Set<AnnotationExpr> annotations(NodeWithAnnotations<?> type,
                                                  Class<? extends Annotation> annotationType)
    {
        var annotations = new HashSet<AnnotationExpr>();
        for (var annotation : type.getAnnotations())
        {
            if (annotation.getName().asString().equals(annotationType.getSimpleName()))
            {
                annotations.add(annotation);
            }
        }
        return annotations;
    }

    /**
     * Returns the boolean value for the given annotation member
     */
    public static boolean booleanValue(AnnotationExpr annotation, String member, boolean defaultValue)
    {
        var expression = expression(annotation, member);
        if (expression instanceof BooleanLiteralExpr)
        {
            return expression.asBooleanLiteralExpr().getValue();
        }
        return defaultValue;
    }

    public static String className(AnnotationExpr annotation, String member)
    {
        var expression = expression(annotation, member);
        if (expression != null && expression.isClassExpr())
        {
            return Names.name(expression.asClassExpr(), Names.Qualification.QUALIFIED, WITHOUT_TYPE_PARAMETERS);
        }
        return null;
    }

    /**
     * Returns the set of class names for the given annotation member
     */
    public static Set<String> classNames(AnnotationExpr annotation, String member)
    {
        var expression = expression(annotation, member);
        if (expression instanceof ArrayInitializerExpr)
        {
            var array = expression.asArrayInitializerExpr();
            var classes = new HashSet<String>();
            for (var value : array.getValues())
            {
                classes.add(Names.name(value.asClassExpr(), Names.Qualification.QUALIFIED, WITHOUT_TYPE_PARAMETERS));
            }
            return classes;
        }
        if (expression instanceof SingleMemberAnnotationExpr)
        {
            var qualifiedName = Names.name(expression.asClassExpr(), Names.Qualification.QUALIFIED, WITHOUT_TYPE_PARAMETERS);
            return qualifiedName == null ? Set.of() : Set.of(qualifiedName);
        }
        return Set.of();
    }

    /**
     * Returns the set of class names for the given annotation
     */
    public static Set<String> classNames(AnnotationExpr annotation)
    {
        if (annotation instanceof MarkerAnnotationExpr)
        {
            return Set.of();
        }

        var expression = annotation.asSingleMemberAnnotationExpr().getMemberValue();
        if (expression instanceof ArrayInitializerExpr)
        {
            var array = expression.asArrayInitializerExpr();
            var classes = new HashSet<String>();
            for (var value : array.getValues())
            {
                var name = Names.name(value.asClassExpr(), UNQUALIFIED, WITHOUT_TYPE_PARAMETERS);
                if (name != null)
                {
                    classes.add(name);
                }
            }
            return classes;
        }

        if (expression instanceof ClassExpr)
        {
            var qualifiedName = Names.name(expression.asClassExpr(), UNQUALIFIED, WITHOUT_TYPE_PARAMETERS);
            return qualifiedName == null ? Set.of() : Set.of(qualifiedName);
        }

        return Set.of();
    }

    /**
     * Returns the expression for the designated annotation member
     */
    public static Expression expression(AnnotationExpr annotation, String member)
    {
        if (annotation instanceof MarkerAnnotationExpr)
        {
            return null;
        }
        for (var pair : annotation.asNormalAnnotationExpr().getPairs())
        {
            if (pair.getName().asString().equals(member))
            {
                var value = pair.getValue();
                if (value != null)
                {
                    return value;
                }
            }
        }
        return null;
    }

    public static boolean hasAnnotation(NodeWithAnnotations<?> type,
                                        Class<? extends Annotation> annotationType)
    {
        return !annotations(type, annotationType).isEmpty();
    }

    public static boolean shouldExcludeType(TypeDeclaration<?> type)
    {
        var marker = false;
        for (var annotation : annotations(type, UmlExcludeType.class))
        {
            var expression = value(annotation, "value");
            if (expression != null)
            {
                var thatTypeName = Names.name(expression.asClassExpr(), UNQUALIFIED, WITHOUT_TYPE_PARAMETERS);
                var thisTypeName = Names.name(type, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS);
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
     * Returns the string value of the given annotation member
     */
    public static String stringValue(AnnotationExpr annotation, String member)
    {
        var expression = expression(annotation, member);
        if (expression instanceof StringLiteralExpr)
        {
            return expression.asStringLiteralExpr().asString();
        }
        if (expression instanceof NameExpr)
        {
            var type = expression.asNameExpr();
            return Names.simpleName(type);
        }
        if (expression instanceof ClassExpr)
        {
            return Names.name(expression.asClassExpr(), Names.Qualification.QUALIFIED, WITHOUT_TYPE_PARAMETERS);
        }
        return null;
    }

    /**
     * Returns the single string value for the given annotation expression
     */
    public static String stringValue(AnnotationExpr annotation)
    {
        var expression = annotation.asSingleMemberAnnotationExpr();
        var value = expression.getMemberValue();
        if (value != null)
        {
            return value.asStringLiteralExpr().asString();
        }
        return null;
    }

    /**
     * Returns the set of string values for the given annotation expression
     */
    @NotNull
    public static Set<String> stringValues(AnnotationExpr annotation, String member)
    {
        var expression = expression(annotation, member);
        if (expression instanceof ArrayInitializerExpr)
        {
            var array = expression.asArrayInitializerExpr();
            var annotations = new HashSet<String>();
            for (var value : array.getValues())
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
     * Returns the value of the given member of the given normal annotation
     */
    public static Expression value(AnnotationExpr annotation, String memberName)
    {
        if (annotation.isMarkerAnnotationExpr())
        {
            return null;
        }

        if (annotation.isSingleMemberAnnotationExpr())
        {
            return annotation.asSingleMemberAnnotationExpr().getMemberValue();
        }

        for (var pair : annotation.asNormalAnnotationExpr().getPairs())
        {
            if (Names.simpleName(pair).equals(memberName))
            {
                return pair.getValue();
            }
        }

        return null;
    }
}
