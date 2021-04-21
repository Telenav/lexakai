package com.telenav.lexakai;

import com.telenav.kivakit.core.filesystem.File;

/**
 * @author jonathanl (shibo)
 */
public class LexakaiProjectFiles
{
    private final LexakaiProject project;

    public LexakaiProjectFiles(final LexakaiProject project)
    {
        this.project = project;
    }

    /**
     * @return The lexakai.properties file for this project in the output tree
     */
    public File lexakaiProperties()
    {
        return project
                .folders()
                .configuration()
                .folder("projects")
                .file(project.properties().projectArtifactId() + ".properties");
    }

    /**
     * @return The lexakai-settings.properties file in the output tree
     */
    public File lexakaiSettings()
    {
        return project
                .folders()
                .configuration()
                .file("lexakai-settings.properties");
    }

    /**
     * @return The project.properties file for this project, from the source tree
     */
    public File projectProperties()
    {
        return project.hasSourceCode()
                ? project.folders().sourceCode().file("project.properties")
                : project.folders().project().file("project.properties");
    }

    /**
     * @return The readme markdown template for this project
     */
    public File readMeTemplate()
    {
        return project
                .folders()
                .configuration()
                .file(project.hasSourceCode()
                        ? "lexakai-source-readme-template.md"
                        : "lexakai-parent-readme-template.md");
    }

    /**
     * @return The readme file in the source code to update
     */
    public File readme()
    {
        return project
                .folders()
                .project()
                .file("README.md");
    }
}
