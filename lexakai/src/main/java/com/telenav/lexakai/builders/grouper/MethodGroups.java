////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jonathanl (shibo)
 */
public class MethodGroups
{
    private final Map<String, MethodGroup> groups = new HashMap<>();

    private final Set<UmlConstructor> constructors = new HashSet<>();

    public void add(String groupName, UmlMethod method)
    {
        groups.computeIfAbsent(groupName, MethodGroup::new).add(method);
    }

    public void add(UmlConstructor constructor)
    {
        constructors.add(constructor);
    }

    public void add(MethodGroup group)
    {
        groups.put(group.name(), group);
    }

    public Iterable<MethodGroup> allGroups()
    {
        var sorted = new ArrayList<>(groups.values());
        Collections.sort(sorted);
        return sorted;
    }

    public List<UmlConstructor> constructors()
    {
        var sorted = new ArrayList<>(constructors);
        Collections.sort(sorted);
        return sorted;
    }

    public boolean isEmpty()
    {
        return groups.isEmpty();
    }

    public MethodGroup largest()
    {
        var maximum = groups.values().stream().max(Comparator.comparingInt(MethodGroup::size));
        return maximum.orElse(null);
    }

    public Iterable<MethodGroup> namedGroups()
    {
        var copy = new HashMap<>(groups);
        copy.remove("static");
        copy.remove("none");
        var sorted = new ArrayList<>(copy.values());
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
