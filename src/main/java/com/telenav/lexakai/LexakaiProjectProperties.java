package com.telenav.lexakai;

import com.telenav.kivakit.core.kernel.language.collections.map.string.VariableMap;
import com.telenav.kivakit.core.kernel.language.paths.StringPath;
import com.telenav.kivakit.core.kernel.language.strings.Packages;
import com.telenav.kivakit.core.resource.resources.other.PropertyMap;
import com.telenav.lexakai.library.Names;
import com.telenav.lexakai.types.UmlType;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.lexakai.library.Names.TypeParameters.WITHOUT_TYPE_PARAMETERS;

/**
 * @author jonathanl (shibo)
 */
public class LexakaiProjectProperties extends PropertyMap
{
    private final LexakaiProject project;

    public LexakaiProjectProperties(final LexakaiProject project)
    {
        this.project = project;

        // Add system and application properties,
        addAll(Lexakai.get().properties());

        // lexakai.settings,
        addAll(PropertyMap.load(project.files().lexakaiSettings()));
        require("lexakai-documentation-location");
        require("lexakai-javadoc-location");
        require("lexakai-images-location");

        // project.properties
        addAll(PropertyMap.load(project.files().projectProperties()));
        final var artifactId = get("project-artifact-id");
        add("project-module-name", artifactId.replaceAll("-", "."));
        require("project-name");
        require("project-version");
        require("project-group-id");
        require("project-artifact-id");

        // lexakai.properties
        addAll(PropertyMap.load(project.files().lexakaiProperties(artifactId)));
        putIfAbsent("project-icon", "gears-32");
        require("project-title");
        require("project-description");
        require("project-icon");

        // project folders,
        add("project-folder", project.folders().project().toString());
        add("project-relative-folder", project.folders().projectRelativeToRoot().toString());
        add("project-output-folder", project.folders().output().toString());
        add("project-output-root-folder", project.folders().outputRoot().toString());

        // and resource locations.
        add("project-diagrams-location", outputDiagramsLocation());
        add("project-documentation-location", outputDocumentationLocation());
        add("project-images-location", imagesLocation());
        add("project-javadoc-location", outputJavadocLocation());
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
        return asPath("lexakai-javadoc-location") + "/" + project.rootProjectName() + "/" + projectModuleName();
    }

    /**
     * @return The location of the given type in this project's Javadoc in the output tree
     */
    public String outputJavadocLocation(final UmlType type)
    {
        final var qualifiedPath = Packages.toPath(type.name(Names.Qualification.QUALIFIED, WITHOUT_TYPE_PARAMETERS));

        // https://www.kivakit.org/javadoc/kivakit/kivakit.core.application/com/telenav/kivakit/core/application/Application.html
        return StringPath.stringPath
                (
                        outputJavadocLocation(),   // https://www.kivakit.org/javadoc/kivakit/kivakit.core.application
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
