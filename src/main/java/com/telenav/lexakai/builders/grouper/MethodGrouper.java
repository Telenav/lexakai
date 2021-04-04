package com.telenav.lexakai.builders.grouper;

import com.telenav.lexakai.members.UmlConstructor;
import com.telenav.lexakai.members.UmlMethod;
import com.telenav.lexakai.types.UmlType;

import java.util.List;

/**
 * @author jonathanl (shibo)
 */
public class MethodGrouper
{
    private final UmlType type;

    private final MethodGroupNameGuesser guesser;

    public MethodGrouper(final UmlType type)
    {
        this.type = type;

        guesser = new MethodGroupNameGuesser(type.diagram().project());
    }

    public MethodGroups groups()
    {
        // Get all included methods in the type,
        final var methods = type.includedMethods();

        // While there are methods to process,
        final var groups = new MethodGroups();
        while (!methods.isEmpty())
        {
            // add the next group of groups,
            final var next = next(methods);
            for (final var at : next.allGroups())
            {
                if (!at.isEmpty())
                {
                    groups.add(at);
                }
            }

            // then remove all the involved methods from those left to process.
            next.allGroups().forEach(group -> group.forEach(methods::remove));
        }

        // then add the constructor group,
        type.type().getConstructors()
                .stream()
                .filter(constructor -> constructor.isPublic() || constructor.isProtected())
                .map(UmlConstructor::new)
                .filter(constructor -> !constructor.isExcluded())
                .forEach(groups::add);

        return groups;
    }

    public MethodGroups next(final List<UmlMethod> methods)
    {
        // For each included method,
        final var groups = new MethodGroups();
        for (final var method : methods)
        {
            // if it is in one or more explicit groups,
            final var groupNames = method.explicitGroupNames();
            if (!groupNames.isEmpty())
            {
                // go through the explicit groups,
                for (final var groupName : groupNames)
                {
                    // and for each method in the type,
                    for (final var at : methods)
                    {
                        // if that method is in the group,
                        if (at.explicitGroupNames().contains(groupName))
                        {
                            // then add it to that group
                            groups.add(groupName, at);
                        }
                    }
                }

                return groups;
            }
            else
            {
                // otherwise add it to all groups we guess it might be in,
                for (final var name : guesser.groupNames(method))
                {
                    groups.add(name, method);
                }
            }
        }

        // Return only the group with the most methods.
        final var largest = groups.largest();
        final var singleton = new MethodGroups();
        singleton.add(largest);
        return singleton;
    }
}
