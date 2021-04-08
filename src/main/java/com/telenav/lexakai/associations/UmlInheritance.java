////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.lexakai.associations;

import com.telenav.lexakai.library.Names;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a UML inheritance arrow.
 *
 * @author jonathanl (shibo)
 */
public class UmlInheritance implements Comparable<UmlInheritance>
{
    /** The superclass type name */
    private final String supertype;

    /** The subclass type name */
    private final String subtype;

    /**
     * An inheritance arrow from the subclass to the superclass
     */
    public UmlInheritance(final String supertype, final String subtype)
    {
        assert supertype != null;
        assert subtype != null;

        this.supertype = supertype;
        this.subtype = subtype;
    }

    @Override
    public int compareTo(@NotNull final UmlInheritance that)
    {
        return uml().compareTo(that.uml());
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof UmlInheritance)
        {
            final UmlInheritance that = (UmlInheritance) object;
            return supertype.equals(that.supertype) && subtype.equals(that.subtype);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(supertype, subtype);
    }

    public String uml()
    {
        return Names.withoutQualification(supertype) + " <|-- " + Names.withoutQualification(subtype);
    }
}
