package com.telenav.lexakai.annotations.visibility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the given class, interface, field or method as not being public API.
 *
 * @author jonathanl (shibo)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
public @interface UmlNotPublicApi
{
}
