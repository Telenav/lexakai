package com.telenav.lexakai.annotations;

import com.telenav.lexakai.annotations.repeaters.UmlMethodGroupRepeater;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds the annotated method to the named method group
 *
 * @author jonathanl (shibo)
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UmlMethodGroupRepeater.class)
@Target({ ElementType.METHOD })
public @interface UmlMethodGroup
{
    String value() default "";
}
