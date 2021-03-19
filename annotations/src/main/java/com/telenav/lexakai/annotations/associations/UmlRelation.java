package com.telenav.lexakai.annotations.associations;

import com.telenav.lexakai.annotations.diagrams.AllDiagrams;
import com.telenav.lexakai.annotations.diagrams.UmlDiagramIdentifier;
import com.telenav.lexakai.annotations.repeaters.UmlRelationRepeater;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jonathanl (shibo)
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UmlRelationRepeater.class)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
public @interface UmlRelation
{
    /**
     * @return Any diagram that this annotation is specific to, or all diagrams if omitted
     */
    Class<? extends UmlDiagramIdentifier> diagram() default AllDiagrams.class;

    /**
     * @return The relation association label
     */
    String label();

    /**
     * @return The cardinality of the "from" end of the relation
     */
    String refereeCardinality() default "";

    /**
     * @return An optional explicit type to refer to, if one cannot be deduced from element this annotation is applied
     * to. For fields and methods, this value is generally not necessary as the member type will be used.
     */
    Class<?> referent() default Void.class;

    /**
     * @return The cardinality of the "to" end of the relation
     */
    String referentCardinality() default "";
}
