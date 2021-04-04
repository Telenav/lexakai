package com.telenav.lexakai.dependencies;

import com.telenav.kivakit.core.kernel.language.objects.Hash;
import com.telenav.kivakit.core.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import org.jetbrains.annotations.NotNull;

/**
 * @author jonathanl (shibo)
 */
public class Artifact implements Comparable<Artifact>
{
    @KivaKitIncludeProperty
    private final int identifier;

    @KivaKitIncludeProperty
    private final String groupId;

    @KivaKitIncludeProperty
    private final String artifactId;

    @KivaKitIncludeProperty
    private final String version;

    public Artifact(final int identifier, final String groupId, final String artifactId, final String version)
    {
        this.identifier = identifier;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String artifactId()
    {
        return artifactId.replaceAll("[-.]", "_");
    }

    @Override
    public int compareTo(@NotNull final Artifact that)
    {
        return dependencyIdentifier().compareTo(that.dependencyIdentifier());
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Artifact)
        {
            final Artifact that = (Artifact) object;
            return identifier == that.identifier;
        }
        return false;
    }

    public String groupId()
    {
        return groupId;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(identifier);
    }

    public int identifier()
    {
        return identifier;
    }

    @Override
    public String toString()
    {
        return dependencyIdentifier();
    }

    public String version()
    {
        return version;
    }

    @NotNull
    private String dependencyIdentifier()
    {
        return groupId + ":" + artifactId + ":" + version;
    }
}
