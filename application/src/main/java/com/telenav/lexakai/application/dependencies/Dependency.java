package com.telenav.lexakai.application.dependencies;

import com.telenav.kivakit.core.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.core.kernel.language.strings.formatting.ObjectFormatter;
import com.telenav.kivakit.core.kernel.messaging.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author jonathanl (shibo)
 */
public class Dependency implements Comparable<Dependency>
{
    @KivaKitIncludeProperty
    private final Artifact from;

    @KivaKitIncludeProperty
    private final Artifact to;

    public Dependency(final Artifact from, final Artifact to)
    {
        assert from != null;
        assert to != null;

        this.from = from;
        this.to = to;
    }

    @Override
    public int compareTo(@NotNull final Dependency that)
    {
        return uml().compareTo(that.uml());
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Dependency)
        {
            final Dependency that = (Dependency) object;
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
        return Message.format("$ --> $", from.artifactId(), to.artifactId());
    }
}
