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

import com.telenav.kivakit.core.collections.Sets;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.language.primitive.Ints;
import com.telenav.kivakit.core.value.count.Count;
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

    public MethodGroup(String name)
    {
        this.name = name;
    }

    public boolean add(UmlMethod method)
    {
        return methods.add(method);
    }

    @Override
    public int compareTo(@NotNull MethodGroup that)
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

        return Ints.inRangeInclusive(name.compareTo(that.name), -1, 1);
    }

    public Count count()
    {
        return Count.count(size());
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof MethodGroup)
        {
            MethodGroup that = (MethodGroup) object;
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

    public void name(String name)
    {
        this.name = name;
    }

    public int size()
    {
        return methods.size();
    }

    public List<UmlMethod> sorted()
    {
        var sorted = new ArrayList<>(methods);
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
        var builder = new StringList();
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
