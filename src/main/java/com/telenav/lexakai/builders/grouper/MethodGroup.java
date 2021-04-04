package com.telenav.lexakai.builders.grouper;

import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.collections.set.Sets;
import com.telenav.kivakit.core.kernel.language.primitives.Ints;
import com.telenav.kivakit.core.kernel.language.values.count.Count;
import com.telenav.lexakai.members.UmlMethod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A set of related {@link UmlMethod}s with a name. Used to implement method groups.
 *
 * @author jonathanl (shibo)
 */
public class MethodGroup implements Comparable<MethodGroup>, Iterable<UmlMethod>
{
    private final Set<UmlMethod> methods = new HashSet<>();

    private String name;

    public MethodGroup(final String name)
    {
        this.name = name;
    }

    public boolean add(final UmlMethod method)
    {
        return methods.add(method);
    }

    @Override
    public int compareTo(@NotNull final MethodGroup that)
    {
        if (name.equals("static"))
        {
            return -4;
        }
        if (name.equals("constructors"))
        {
            return -3;
        }
        if (name.equals("none"))
        {
            return -2;
        }

        return Ints.inRange(name.compareTo(that.name), -1, 1);
    }

    public Count count()
    {
        return Count.count(size());
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof MethodGroup)
        {
            final MethodGroup that = (MethodGroup) object;
            return name.equals(that.name);
        }
        return false;
    }

    public UmlMethod first()
    {
        return Sets.first(methods);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }

    public boolean isEmpty()
    {
        return methods.isEmpty();
    }

    @NotNull
    @Override
    public Iterator<UmlMethod> iterator()
    {
        return methods.iterator();
    }

    public String name()
    {
        return name;
    }

    public void name(final String name)
    {
        this.name = name;
    }

    public int size()
    {
        return methods.size();
    }

    public List<UmlMethod> sorted()
    {
        final var sorted = new ArrayList<>(methods);
        Collections.sort(sorted);
        return sorted;
    }

    @Override
    public String toString()
    {
        return uml();
    }

    public String uml()
    {
        final var builder = new StringList();
        if (name.equals("none"))
        {
            builder.append("--");
        }
        else
        {
            builder.append("--" + name + "--");
        }
        sorted().forEach(method -> builder.add(method.uml()));
        return builder.join("\n");
    }
}

