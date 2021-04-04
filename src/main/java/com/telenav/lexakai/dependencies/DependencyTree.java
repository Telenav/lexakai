package com.telenav.lexakai.dependencies;

import com.telenav.kivakit.core.filesystem.Folder;

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

    public DependencyTree(final String artifactId, final Folder projectFolder)
    {
        this.artifactId = artifactId;
        this.projectFolder = projectFolder;
    }

    public void add(final Dependency dependency)
    {
        dependencies.add(dependency);
    }

    public void add(final Artifact artifact)
    {
        artifacts.add(artifact);
        identifierToArtifact.put(artifact.identifier(), artifact);
    }

    public Artifact artifact(final int identifier)
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

