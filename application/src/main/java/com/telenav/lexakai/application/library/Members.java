package com.telenav.lexakai.application.library;

import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.associations.UmlComposition;
import com.telenav.lexakai.annotations.associations.UmlRelation;

import java.lang.annotation.Annotation;

/**
 * @author jonathanl (shibo)
 */
public class Members
{
    /**
     * @return The association string for the given method and member
     */
    public static String associationString(final NodeWithAnnotations<?> member,
                                           final Class<? extends Annotation> annotation,
                                           final String key)
    {
        final var expression = member.getAnnotationByClass(annotation);
        if (expression.isPresent())
        {
            return Annotations.stringValue(expression.get(), key);
        }
        return null;
    }

    /**
     * @return Any relation value for the given key on the given member
     */
    public static String associationString(final NodeWithAnnotations<?> member, final String key)
    {
        final var aggregation = member.getAnnotationByClass(UmlAggregation.class);
        if (aggregation.isPresent())
        {
            return Annotations.stringValue(aggregation.get(), key);
        }

        final var composition = member.getAnnotationByClass(UmlComposition.class);
        if (composition.isPresent())
        {
            return Annotations.stringValue(composition.get(), key);
        }

        final var relation = member.getAnnotationByClass(UmlRelation.class);
        if (relation.isPresent())
        {
            return Annotations.stringValue(relation.get(), key);
        }

        return null;
    }
}
