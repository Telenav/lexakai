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

package com.telenav.lexakai.dependencies;

import com.telenav.kivakit.core.string.ObjectFormatter;
import com.telenav.kivakit.core.language.reflection.property.IncludeProperty;
import com.telenav.kivakit.core.string.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author jonathanl (shibo)
 */
public class Dependency implements Comparable<Dependency>
{
    @IncludeProperty
    private final Artifact from;

    @IncludeProperty
    private final Artifact to;

    public Dependency(Artifact from, Artifact to)
    {
        assert from != null;
        assert to != null;

        this.from = from;
        this.to = to;
    }

    @Override
    public int compareTo(@NotNull Dependency that)
    {
        return uml().compareTo(that.uml());
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Dependency)
        {
            Dependency that = (Dependency) object;
            return from.equals(that.from) && to.equals(that.to);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(from, to);
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }

    public String uml()
    {
        return Strings.format("$ --> $", from.artifactId(), to.artifactId());
    }
}
