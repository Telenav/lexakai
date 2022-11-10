////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2022 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    public MethodGrouper(UmlType type)
    {
        this.type = type;

        guesser = new MethodGroupNameGuesser(type.diagram().project());
    }

    public MethodGroups groups()
    {
        // Get all included methods in the type,
        var methods = type.includedMethods();

        // While there are methods to process,
        var groups = new MethodGroups();
        while (!methods.isEmpty())
        {
            // add the next group of groups,
            var next = next(methods);
            for (var at : next.allGroups())
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

    public MethodGroups next(List<UmlMethod> methods)
    {
        // For each included method,
        var groups = new MethodGroups();
        for (var method : methods)
        {
            // if it is in one or more explicit groups,
            var groupNames = method.explicitGroupNames();
            if (!groupNames.isEmpty())
            {
                // go through the explicit groups,
                for (var groupName : groupNames)
                {
                    // and for each method in the type,
                    for (var at : methods)
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
                for (var name : guesser.groupNames(method))
                {
                    groups.add(name, method);
                }
            }
        }

        // Return only the group with the most methods.
        var largest = groups.largest();
        var singleton = new MethodGroups();
        singleton.add(largest);
        return singleton;
    }
}
