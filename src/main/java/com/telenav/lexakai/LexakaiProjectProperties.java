package com.telenav.lexakai;

import com.telenav.kivakit.core.collections.map.VariableMap;
import com.telenav.kivakit.core.string.Packages;
import com.telenav.kivakit.core.string.Paths;
import com.telenav.kivakit.properties.PropertyMap;
import com.telenav.lexakai.library.Names;
import com.telenav.lexakai.types.UmlType;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.lexakai.library.Names.TypeParameters.WITHOUT_TYPE_PARAMETERS;

/**
 * @author jonathanl (shibo)
 */
@SuppressWarnings({ "unused", "SpellCheckingInspection" })
public class LexakaiProjectProperties extends PropertyMap
{
    public static LexakaiProjectProperties load(LexakaiProject project)
    {
        // Create project properties for the given project,
        var properties = new LexakaiProjectProperties(project);

        // add system and application properties,
        properties.addAll(Lexakai.get().properties());

        // add maven coordinates,
        var coordinates = project.mavenCoordinates();
        properties.put("project-name", coordinates.artifactId().text());
        properties.put("project-group-id", coordinates.groupId().text());
        properties.put("project-artifact-id", coordinates.artifactId().text());
        properties.put("project-version", coordinates.version().text());
        properties.add("project-module-name", coordinates.artifactId().text().replaceAll("-", "."));

        var artifactId = properties.get("project-artifact-id");
        if (!artifactId.contains("superpom"))
        {
            // check that we have Maven coordinate information,
            properties.require("project-name");
            properties.require("project-version");
            properties.require("project-group-id");
            properties.require("project-artifact-id");

            // add lexakai.settings,
            properties.addAll(PropertyMap.load(project, project.files().lexakaiSettings()).expandedWith(properties));
            properties.require("lexakai-documentation-location");
            properties.require("lexakai-javadoc-location");
            properties.require("lexakai-images-location");

            // add lexakai.properties
            var lexakaiProperties = project.files().lexakaiProperties(artifactId);
            if (!lexakaiProperties.exists())
            {
                project.lexakai().exit("Lexakai properties not found: $", lexakaiProperties);
            }
            properties.addAll(PropertyMap.load(project, lexakaiProperties).expandedWith(properties));
            properties.putIfAbsent("project-icon", "gears-32");

            // check that we have a project title, description and icon,
            properties.require("project-title");
            properties.require("project-description");
            properties.require("project-icon");

            // add project folders,
            properties.add("project-folder", project.folders().project().toString());
            properties.add("project-relative-folder", project.folders().projectRelativeToRoot().toString());
            properties.add("project-output-folder", project.folders().output().toString());
            properties.add("project-output-root-folder", project.folders().outputRoot().path().asContraction(80));

            // and add resource locations.
            properties.add("project-diagrams-location", properties.outputDiagramsLocation());
            properties.add("project-documentation-location", properties.outputDocumentationLocation());
            properties.add("project-images-location", properties.imagesLocation());
            properties.add("project-javadoc-location", properties.outputJavadocLocation());

            return properties;
        }

        return null;
    }

    private final LexakaiProject project;

    protected LexakaiProjectProperties(LexakaiProject project)
    {
        this.project = project;
    }

    @Override
    public VariableMap<String> add(String key, String value)
    {
        var map = super.add(key, value);
        require(key);
        return map;
    }

    public String imagesLocation()
    {
        return asPathString("lexakai-images-location");
    }

    public String outputDiagramsLocation()
    {
        var output = outputDocumentationLocation();
        if (output != null)
        {
            return Paths.pathConcatenate(output, project.rootProjectName(), project.folders().projectRelativeToRoot().toString(), "documentation/diagrams");
        }
        return null;
    }

    public String outputDocumentationLocation()
    {
        return asPathString("lexakai-documentation-location");
    }

    /**
     * @return The location of this project's Javadoc in the output tree
     */
    public String outputJavadocLocation()
    {
        var location = asPathString("lexakai-javadoc-location") + "/" + project.rootProjectName();
        var moduleName = projectModuleName();
        if (moduleName != null && !moduleName.equalsIgnoreCase("none"))
        {
            location += "/" + moduleName;
        }
        return location;
    }

    /**
     * @return The location of the given type in this project's Javadoc in the output tree
     */
    @SuppressWarnings("ClassEscapesDefinedScope")
    public String outputJavadocLocation(UmlType type)
    {
        var qualifiedPath = Packages.packageToPath(type.name(Names.Qualification.QUALIFIED, WITHOUT_TYPE_PARAMETERS));

        // https://www.kivakit.org/javadoc/kivakit/kivakit.application/com/telenav/kivakit/core/application/Application.html
        return Paths.pathConcatenate
                (
                        outputJavadocLocation(),        // https://www.kivakit.org/javadoc/kivakit/kivakit.application
                        qualifiedPath + ".html"   // com/kivakit/core/application/Application.html
                );
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

    private void require(String key)
    {
        var value = get(key);
        ensure(value != null && !value.contains("[UNDEFINED]"),
                "Project $: The required key '$' is not defined", project.name(), key);
    }
}
