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
    private final Folder outputRootFolder;

    public LexakaiProjectFolders(final LexakaiProject project,
                                 final Folder root,
                                 final Folder projectFolder,
                                 final Folder outputRootFolder)
    {
        this.project = project;
        this.root = root;
        this.projectFolder = projectFolder;
        this.outputRootFolder = outputRootFolder;
    }

    /**
     * @return The images folder
     */
    public Folder images()
    {
        return project.properties().asFolder("lexakai-images-folder");
    }

    /**
     * @return The output folder for this project
     */
    public Folder output()
    {
        return project.folders().outputRoot().folder(sourceRelativeProject());
    }

    /**
     * @return The folder where diagrams are output
     */
    public Folder outputDiagrams()
    {
        return outputDocumentation().folder("diagrams");
    }

    /**
     * @return The folder where documentation is stored
     */
    public Folder outputDocumentation()
    {
        return output().folder("documentation").mkdirs();
    }

    /**
     * @return The folder for lexakai output
     */
    public Folder outputLexakai()
    {
        return outputDocumentation().folder("lexakai").mkdirs();
    }

    /**
     * @return The root output folder
     */
    public Folder outputRoot()
    {
        return outputRootFolder;
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
    public Folder sourceFolder()
    {
        return project.folders().sourceProject().folder("src/main/java");
    }

    /**
     * @return This project's folder in the source tree
     */
    public Folder sourceProject()
    {
        return projectFolder;
    }

    /**
     * @return This folder in the source tree for this project, relative to the root folder
     */
    public Folder sourceRelativeProject()
    {
        return project.folders().sourceProject().relativeTo(project.folders().root());
    }
}
