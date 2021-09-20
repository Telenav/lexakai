package com.telenav.lexakai;

import com.telenav.kivakit.kernel.language.collections.map.string.VariableMap;
import com.telenav.kivakit.kernel.language.paths.StringPath;
import com.telenav.kivakit.kernel.language.strings.Packages;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.resource.resources.other.PropertyMap;
import com.telenav.lexakai.library.Names;
import com.telenav.lexakai.types.UmlType;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.lexakai.library.Names.TypeParameters.WITHOUT_TYPE_PARAMETERS;

/**
 * @author jonathanl (shibo)
 */
public class LexakaiProjectProperties extends PropertyMap
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static LexakaiProjectProperties load(final LexakaiProject project)
    {
        final var properties = new LexakaiProjectProperties(project);

        // Add system and application properties,
        final var systemProperties = Lexakai.get().properties();
        properties.addAll(systemProperties);

        // project.properties
        final var projectProperties = project.files().projectProperties();
        if (!projectProperties.exists())
        {
            project.lexakai().exit("Project.properties not found: $", projectProperties);
        }
        properties.addAll(PropertyMap.load(LOGGER, projectProperties).expandedWith(properties));
        final var artifactId = properties.get("project-artifact-id");
        if (artifactId.contains("superpom"))
        {
            return null;
        }
        properties.add("project-module-name", artifactId.replaceAll("-", "."));
        properties.require("project-name");
        properties.require("project-version");
        properties.require("project-group-id");
        properties.require("project-artifact-id");

        // lexakai.settings,
        properties.addAll(PropertyMap.load(LOGGER, project.files().lexakaiSettings()).expandedWith(properties));
        properties.require("lexakai-documentation-location");
        properties.require("lexakai-javadoc-location");
        properties.require("lexakai-images-location");

        // lexakai.properties
        final var lexakaiProperties = project.files().lexakaiProperties(artifactId);
        if (!lexakaiProperties.exists())
        {
            project.lexakai().exit("Lexakai.properties not found: $", lexakaiProperties);
        }
        properties.addAll(PropertyMap.load(LOGGER, lexakaiProperties).expandedWith(properties));
        properties.putIfAbsent("project-icon", "gears-32");
        properties.require("project-title");
        properties.require("project-description");
        properties.require("project-icon");

        // project folders,
        properties.add("project-folder", project.folders().project().toString());
        properties.add("project-relative-folder", project.folders().projectRelativeToRoot().toString());
        properties.add("project-output-folder", project.folders().output().toString());
        properties.add("project-output-root-folder", project.folders().outputRoot().toString());

        // and resource locations.
        properties.add("project-diagrams-location", properties.outputDiagramsLocation());
        properties.add("project-documentation-location", properties.outputDocumentationLocation());
        properties.add("project-images-location", properties.imagesLocation());
        properties.add("project-javadoc-location", properties.outputJavadocLocation());

        return properties;
    }

    private final LexakaiProject project;

    protected LexakaiProjectProperties(final LexakaiProject project)
    {
        this.project = project;
    }

    @Override
    public VariableMap<String> add(final String key, final String value)
    {
        final var map = super.add(key, value);
        require(key);
        return map;
    }

    public String imagesLocation()
    {
        return asPath("lexakai-images-location");
    }

    public String outputDiagramsLocation()
    {
        final var output = outputDocumentationLocation();
        return output == null
                ? null
                : StringPath.stringPath
                        (
                                output,
                                project.rootProjectName(),
                                project.folders().projectRelativeToRoot().toString(),
                                "documentation/diagrams"
                        )
                .toString();
    }

    public String outputDocumentationLocation()
    {
        return asPath("lexakai-documentation-location");
    }

    /**
     * @return The location of this project's Javadoc in the output tree
     */
    public String outputJavadocLocation()
    {
        var location = asPath("lexakai-javadoc-location") + "/" + project.rootProjectName();
        final var moduleName = projectModuleName();
        if (moduleName != null && !moduleName.equalsIgnoreCase("none"))
        {
            location += "/" + moduleName;
        }
        return location;
    }

    /**
     * @return The location of the given type in this project's Javadoc in the output tree
     */
    public String outputJavadocLocation(final UmlType type)
    {
        final var qualifiedPath = Packages.toPath(type.name(Names.Qualification.QUALIFIED, WITHOUT_TYPE_PARAMETERS));

        // https://www.kivakit.org/javadoc/kivakit/kivakit.application/com/telenav/kivakit/core/application/Application.html
        return StringPath.stringPath
                        (
                                outputJavadocLocation(),   // https://www.kivakit.org/javadoc/kivakit/kivakit.application
                                qualifiedPath + ".html"    // com/kivakit/core/application/Application.html
                        )
                .toString();
    }

    public String projectArtifactId()
    {
        return get("project-artifact-id");
    }

    public String projectModuleName()
    {
        return get("project-module-name");
    }

    public String projectName()
    {
        return get("project-name");
    }

    private void require(final String key)
    {
        final var value = get(key);
        ensure(value != null && !value.contains("[UNDEFINED]"),
                "Project $: The required key '$' is not defined", project.name(), key);
    }
}
