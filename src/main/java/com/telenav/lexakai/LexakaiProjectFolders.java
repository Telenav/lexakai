package com.telenav.lexakai;

import com.telenav.kivakit.core.filesystem.Folder;

/**
 * @author jonathanl (shibo)
 */
public class LexakaiProjectFolders
{
    private final LexakaiProject project;

    /** The root folder that is being processed */
    private final Folder root;

    /** The project folder under the root folder for this project */
    private final Folder projectFolder;

    /** The root output folder */
    private final Folder outputRoot;

    public LexakaiProjectFolders(final LexakaiProject project,
                                 final Folder root,
                                 final Folder projectFolder,
                                 final Folder outputRoot)
    {
        this.project = project;
        this.root = root;
        this.projectFolder = projectFolder;
        this.outputRoot = outputRoot;
    }

    /**
     * @return The folder where lexakai configuration is stored in the source tree
     */
    public Folder configuration()
    {
        return project().folder("documentation/lexakai");
    }

    /**
     * @return The folder where diagrams are output
     */
    public Folder diagramOutput()
    {
        return documentationOutput().folder("diagrams");
    }

    /**
     * @return The folder where documentation is stored
     */
    public Folder documentationOutput()
    {
        return output().folder("documentation").mkdirs();
    }

    /**
     * @return The folder for lexakai output
     */
    public Folder lexakaiOutput()
    {
        return documentationOutput().folder("lexakai").mkdirs();
    }

    /**
     * @return The output folder for this project
     */
    public Folder output()
    {
        return project.folders().outputRoot().folder(projectRelativeToRoot());
    }

    /**
     * @return The root output folder
     */
    public Folder outputRoot()
    {
        return outputRoot;
    }

    /**
     * @return This project's folder in the source tree
     */
    public Folder project()
    {
        return projectFolder;
    }

    /**
     * @return This folder in the source tree for this project, relative to the root folder
     */
    public Folder projectRelativeToRoot()
    {
        return project().relativeTo(root());
    }

    /**
     * @return The root folder that Lexakai is processing
     */
    public Folder root()
    {
        return root;
    }

    /**
     * @return The source folder for this project, if the {@link LexakaiProject#hasSourceCode()} method returns true
     */
    public Folder sourceCode()
    {
        return project.folders().project().folder("src/main/java");
    }
}
