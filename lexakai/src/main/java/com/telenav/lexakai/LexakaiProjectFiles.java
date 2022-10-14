package com.telenav.lexakai;

import com.telenav.kivakit.filesystem.File;

/**
 * @author jonathanl (shibo)
 */
@SuppressWarnings({ "SpellCheckingInspection", "unused" })
public class LexakaiProjectFiles
{
    private final LexakaiProject project;

    public LexakaiProjectFiles(LexakaiProject project)
    {
        this.project = project;
    }

    /**
     * Returns the lexakai.groups file for automatic method grouping
     */
    public File lexakaiGroups()
    {
        return folders()
                .settings()
                .file("lexakai.groups");
    }

    /**
     * Returns the lexakai.properties file for this project in the output tree
     */
    public File lexakaiProperties()
    {
        var properties = project.properties();
        return properties == null ? null : lexakaiProperties(properties.projectArtifactId());
    }

    /**
     * Returns the lexakai.properties file for the given project in the output tree
     */
    public File lexakaiProperties(String artifactId)
    {
        return folders()
                .settings()
                .folder("projects")
                .file(artifactId + ".properties");
    }

    /**
     * Returns the lexakai.settings file in the output tree
     */
    public File lexakaiSettings()
    {
        return folders()
                .settings()
                .file("lexakai.settings");
    }

    /**
     * Returns the lexakai.theme file for diagrams
     */
    public File lexakaiTheme()
    {
        return folders()
                .settings()
                .file("lexakai.theme");
    }

    /**
     * Returns the project.properties file for this project, from the source tree
     */
    public File projectProperties()
    {
        return project.hasSourceCode()
                ? folders().sourceCode().file("project.properties")
                : folders().project().file("project.properties");
    }

    /**
     * Returns the readme markdown template for this project
     */
    public File readMeTemplate()
    {
        return folders()
                .settings()
                .file(project.hasSourceCode()
                        ? "lexakai-source-readme-template.md"
                        : "lexakai-parent-readme-template.md");
    }

    /**
     * Returns the readme file in the source code to update
     */
    public File readme()
    {
        return folders()
                .project()
                .file("README.md");
    }

    private LexakaiProjectFolders folders()
    {
        return project.folders();
    }
}
