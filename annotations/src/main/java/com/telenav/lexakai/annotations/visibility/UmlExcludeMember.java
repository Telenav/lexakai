package com.telenav.lexakai.annotations.visibility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes field, method or constructor from UML diagrams.
 *
 * @author jonathanl (shibo)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface UmlExcludeMember
{
}
