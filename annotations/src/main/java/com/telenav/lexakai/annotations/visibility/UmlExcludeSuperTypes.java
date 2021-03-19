package com.telenav.lexakai.annotations.visibility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes a superclass type from UML diagrams.
 *
 * @author jonathanl (shibo)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface UmlExcludeSuperTypes
{
    Class<?>[] value() default {};
}
