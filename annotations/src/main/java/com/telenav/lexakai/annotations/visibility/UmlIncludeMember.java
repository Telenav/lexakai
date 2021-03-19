package com.telenav.lexakai.annotations.visibility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Includes the given field, method or constructor even if it is excluded for some other reason
 *
 * @author jonathanl (shibo)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface UmlIncludeMember
{
}
