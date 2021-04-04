package com.telenav.lexakai.members;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.strings.Strings;
import com.telenav.lexakai.library.Methods;
import com.telenav.lexakai.library.Name;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a UML method declaration.
 *
 * @author jonathanl (shibo)
 */
public class UmlMethod implements Comparable<UmlMethod>
{
    /** The parent in which this method is declared */
    private final ClassOrInterfaceDeclaration parent;

    /** The method declaration */
    private final MethodDeclaration method;

    /** The set of method groups that this method belongs to */
    private final Set<String> explicitGroups;

    public UmlMethod(final ClassOrInterfaceDeclaration parent, final MethodDeclaration method)
    {
        this.parent = parent;
        this.method = method;

        explicitGroups = Methods.explicitGroups(method);
    }

    @Override
    public int compareTo(@NotNull final UmlMethod that)
    {
        final int compare = sortOrder() - that.sortOrder();
        if (compare == 0)
        {
            return name().compareTo(that.name());
        }
        return compare;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof UmlMethod)
        {
            final UmlMethod that = (UmlMethod) object;
            return uml().equals(that.uml());
        }
        return false;
    }

    /**
     * @return The set of groups that this method belongs to
     */
    public Set<String> explicitGroupNames()
    {
        return explicitGroups;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uml());
    }

    /**
     * @return True if this method is excluded from all diagrams
     */
    public boolean isExcluded()
    {
        return Methods.isExcluded(method);
    }

    public boolean isOverride()
    {
        return method.getAnnotationByClass(Override.class).isPresent();
    }

    public boolean isProtected()
    {
        return method.isProtected();
    }

    public boolean isPublic()
    {
        return method.isPublic();
    }

    public MethodDeclaration method()
    {
        return method;
    }

    public String name()
    {
        return method.getName().asString();
    }

    public ClassOrInterfaceDeclaration parent()
    {
        return parent;
    }

    /**
     * @return The return type of this method
     */
    public String returnType()
    {
        return method.getType().asString();
    }

    public String simpleName()
    {
        return Name.simpleName(method);
    }

    @Override
    public String toString()
    {
        return uml();
    }

    public String uml()
    {
        final var methodName = name();
        final var override = isOverride() ? "^" : "";
        final var protection = (parent.isInterface() || isPublic()) ? "+" : "#";
        final var returnType = method.getType();
        final var parameters = method.getParameters();
        final var list = new StringList();
        for (final var parameter : parameters)
        {
            var parameterType = parameter.getType().asString();
            if (parameter.isVarArgs())
            {
                parameterType += "...";
            }
            final var parameterName = parameter.getName().asString();
            if (Strings.containsIgnoreCase(parameterType, parameterName))
            {
                list.add(parameterType);
            }
            else
            {
                list.add(parameterType + " " + parameterName);
            }
        }
        return protection + override + returnType.asString() + " " + methodName + "(" + list.join(", ") + ")";
    }

    private int sortOrder()
    {
        if (isOverride())
        {
            return 2;
        }
        if (isProtected())
        {
            return 1;
        }
        if (isPublic())
        {
            return 0;
        }
        return -1;
    }
}
