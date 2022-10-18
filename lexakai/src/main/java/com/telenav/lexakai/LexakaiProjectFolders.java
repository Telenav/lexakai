package com.telenav.lexakai;

import com.telenav.kivakit.filesystem.Folder;

/**
 * @author jonathanl (shibo)
 */
public class LexakaiProjectFolders
{
    private final LexakaiProject lexakaiProject;

    /** The root folder that is being processed */
    private final Folder root;

    /** The project folder under the root folder for this project */
    private final Folder project;

    /** The root output folder */
    private final Folder outputRoot;

    public LexakaiProjectFolders(LexakaiProject lexakaiProject,
                                 Folder root,
                                 Folder project,
                                 Folder outputRoot)
    {
        this.lexakaiProject = lexakaiProject;
        this.root = root;
        this.project = project;
        this.outputRoot = outputRoot;
    }

    /**
     * Returns the folder where diagrams are output
     */
    public Folder diagramOutput()
    {
        return documentationOutput().folder("diagrams").mkdirs();
    }

    /**
     * Returns the folder where documentation is stored
     */
    public Folder documentationOutput()
    {
        return output().folder("documentation").mkdirs();
    }

    /**
     * Returns the folder for lexakai output
     */
    public Folder lexakaiOutput()
    {
        return documentationOutput().folder("lexakai").mkdirs();
    }

    /**
     * Returns the output folder for this project
     */
    public Folder output()
    {
        return lexakaiProject.folders().outputRoot().folder(projectRelativeToRoot()).mkdirs();
    }

    /**
     * Returns the root output folder
     */
    public Folder outputRoot()
    {
        return outputRoot.mkdirs();
    }

    /**
     * Returns this project's folder in the source tree
     */
    public Folder project()
    {
        return project;
    }

    /**
     * Returns this folder in the source tree for this project, relative to the root folder
     */
    public Folder projectRelativeToRoot()
    {
        return project().relativeTo(root());
    }

    /**
     * Returns the root folder that Lexakai is processing
     */
    public Folder root()
    {
        return root;
    }

    /**
     * Returns the folder where lexakai configuration is stored in the source tree
     */
    public Folder settings()
    {
        return root().folder("documentation/lexakai").mkdirs();
    }

    /**
     * Returns the source folder for this project, if the {@link LexakaiProject#hasSourceCode()} method returns true
     */
    public Folder sourceCode()
    {
        return lexakaiProject.folders().project().folder("src/main/java");
    }
}
