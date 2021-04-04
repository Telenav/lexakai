package com.telenav.lexakai.builders.grouper;

import com.telenav.lexakai.members.UmlConstructor;
import com.telenav.lexakai.members.UmlMethod;

import java.util.*;

/**
 * @author jonathanl (shibo)
 */
public class MethodGroups
{
    private final Map<String, MethodGroup> groups = new HashMap<>();

    private final Set<UmlConstructor> constructors = new HashSet<>();

    public void add(final String groupName, final UmlMethod method)
    {
        groups.computeIfAbsent(groupName, MethodGroup::new).add(method);
    }

    public void add(final UmlConstructor constructor)
    {
        constructors.add(constructor);
    }

    public void add(final MethodGroup group)
    {
        groups.put(group.name(), group);
    }

    public Iterable<MethodGroup> allGroups()
    {
        final var sorted = new ArrayList<>(groups.values());
        Collections.sort(sorted);
        return sorted;
    }

    public List<UmlConstructor> constructors()
    {
        final var sorted = new ArrayList<>(constructors);
        Collections.sort(sorted);
        return sorted;
    }

    public boolean isEmpty()
    {
        return groups.isEmpty();
    }

    public MethodGroup largest()
    {
        final var maximum = groups.values().stream().max(Comparator.comparingInt(MethodGroup::size));
        return maximum.isPresent() ? maximum.get() : null;
    }

    public Iterable<MethodGroup> namedGroups()
    {
        final var copy = new HashMap<>(groups);
        copy.remove("static");
        copy.remove("none");
        final var sorted = new ArrayList<>(copy.values());
        Collections.sort(sorted);
        return sorted;
    }

    public MethodGroup none()
    {
        return groups.computeIfAbsent("none", MethodGroup::new);
    }

    public MethodGroup staticMethods()
    {
        return groups.computeIfAbsent("static", MethodGroup::new);
    }
}
