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

import com.telenav.kivakit.core.language.Hash;
import com.telenav.kivakit.core.language.reflection.property.KivaKitIncludeProperty;
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

    public Artifact(int identifier, String groupId, String artifactId, String version)
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
    public int compareTo(@NotNull Artifact that)
    {
        return dependencyIdentifier().compareTo(that.dependencyIdentifier());
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Artifact)
        {
            Artifact that = (Artifact) object;
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
        return Hash.hashMany(identifier);
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
