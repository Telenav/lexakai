package com.telenav.lexakai.application.associations;

import com.telenav.lexakai.application.library.Name;
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
        return Name.withoutQualification(supertype) + " <|-- " + Name.withoutQualification(subtype);
    }
}
