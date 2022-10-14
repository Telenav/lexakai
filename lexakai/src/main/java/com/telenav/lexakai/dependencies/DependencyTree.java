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

import com.telenav.kivakit.filesystem.Folder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jonathanl (shibo)
 */
public class DependencyTree
{
    private final Set<Artifact> artifacts = new HashSet<>();

    private final Set<Dependency> dependencies = new HashSet<>();

    private final Map<Integer, Artifact> identifierToArtifact = new HashMap<>();

    private final String artifactId;

    private final Folder projectFolder;

    public DependencyTree(String artifactId, Folder projectFolder)
    {
        this.artifactId = artifactId;
        this.projectFolder = projectFolder;
    }

    public void add(Dependency dependency)
    {
        dependencies.add(dependency);
    }

    public void add(Artifact artifact)
    {
        artifacts.add(artifact);
        identifierToArtifact.put(artifact.identifier(), artifact);
    }

    public Artifact artifact(int identifier)
    {
        return identifierToArtifact.get(identifier);
    }

    public String artifactId()
    {
        return artifactId;
    }

    public Set<Artifact> artifacts()
    {
        return artifacts;
    }

    public Set<Dependency> dependencies()
    {
        return dependencies;
    }

    public boolean isEmpty()
    {
        return artifacts.isEmpty();
    }

    public Folder projectFolder()
    {
        return projectFolder;
    }

    public String title()
    {
        return artifactId;
    }
}
