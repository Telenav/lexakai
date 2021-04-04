package com.telenav.lexakai.application.indexes;

import com.telenav.kivakit.core.filesystem.File;
import com.telenav.kivakit.core.filesystem.Folder;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.core.kernel.language.strings.Strings;
import com.telenav.kivakit.core.kernel.language.strings.Wrap;
import com.telenav.kivakit.core.kernel.language.time.LocalTime;
import com.telenav.kivakit.core.resource.resources.packaged.Package;
import com.telenav.kivakit.core.resource.resources.packaged.PackageResource;
import com.telenav.kivakit.core.resource.resources.string.StringResource;
import com.telenav.lexakai.application.LexakaiProject;
import com.telenav.lexakai.application.library.Name;
import com.telenav.lexakai.application.types.UmlType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static com.telenav.kivakit.core.resource.CopyMode.UPDATE;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;

/**
 * @author jonathanl (shibo)
 */
public class ReadMeIndexUpdater
{
    private static final String SOURCE_INDEX = PackageResource.packageResource(ReadMeIndexUpdater.class, "ReadMeSourceIndex.md").reader().string();

    private static final String PARENT_INDEX = PackageResource.packageResource(ReadMeIndexUpdater.class, "ReadMeProjectIndex.md").reader().string();

    private final LexakaiProject project;

    private final Pattern SECTION_HEADING = Pattern.compile("^### ([ A-Za-z0-9_-]+)(\\s*<a name)?", MULTILINE);

    public ReadMeIndexUpdater(final LexakaiProject project)
    {
        this.project = project;
    }

    public void update(final Pattern javadocSectionPattern, final List<Folder> childProjects, boolean addHtmlAnchors)
    {
        // Get any user text blocks from any existing read me file,
        final var index = new StringList();
        final var blocks = userTextBlocks(project.readmeFile());
        final var topBlock = index(blocks.getOrDefault(0, ""), index, addHtmlAnchors);
        final var bottomBlock = index(blocks.getOrDefault(1, ""), index, addHtmlAnchors);

        // create a variable map for the readme template,
        final var variables = project.properties();
        variables.put("user-text-top", topBlock);
        variables.put("user-text-bottom", bottomBlock);
        variables.put("project-index", index.join("  \n") + (index.isEmpty() ? "" : "  "));
        variables.put("date", LocalTime.now().asDateString());
        variables.put("time", LocalTime.now().asTimeString());

        // and if the project has source code,
        if (project.hasSourceCode())
        {
            // compose diagram links,
            final var sections = new StringList();
            final var types = new HashSet<UmlType>();
            final var classDiagramIndex = new StringList();
            final var packageDiagramIndex = new StringList();
            project.diagrams(diagram ->
            {
                final var line = "[*" + diagram.title() + "*](documentation/diagrams/" + diagram.identifier() + ".svg)  ";
                (diagram.isPackageDiagram() ? packageDiagramIndex : classDiagramIndex).add(line);
                types.addAll(diagram.includedQualifiedTypes());
            });
            if (classDiagramIndex.isEmpty())
            {
                classDiagramIndex.add("None");
            }
            if (packageDiagramIndex.isEmpty())
            {
                packageDiagramIndex.add("None");
            }

            // and javadoc sections,
            final var sorted = new ArrayList<>(types);
            sorted.sort(Comparator.comparing(type -> type.name(Name.Qualification.UNQUALIFIED, Name.TypeParameters.WITHOUT_TYPE_PARAMETERS)));
            sorted.forEach(type -> sections.addAll(sections(type, javadocSectionPattern)));

            // populate the variable map with this information,
            variables.put("class-diagram-index", classDiagramIndex.join("\n"));
            variables.put("package-diagram-index", packageDiagramIndex.join("\n"));
            variables.put("key-documentation", sections.join("\n"));

            // wrap the project description
            final var description = variables.get("project-description");
            if (description.length() > 120)
            {
                final var wrapped = Wrap.wrap(description, 100).replaceAll("\n", "  \n");
                variables.put("project-description", wrapped);
            }
        }
        else
        {
            // otherwise, compose the list of child projects, if any,
            final var childProjectMarkdown = new StringList();
            if (!childProjects.isEmpty())
            {
                childProjects.forEach(at -> childProjectMarkdown.add("[**" + at.name().name() + "**](" + at.name().name() + "/README.md)  "));
            }

            // and populate the variable map with this information,
            variables.put("child-projects", childProjectMarkdown.join("\n"));
        }

        // then write the interpolated template,
        new StringResource(variables.expanded(project.hasSourceCode() ? SOURCE_INDEX : PARENT_INDEX)).safeCopyTo(project.readmeFile(), UPDATE, ProgressReporter.NULL);

        // and finally, update the referenced images.
        final var images = Package.of(getClass(), "documentation/images");
        final var imagesFolder = project.imagesFolder().absolute().mkdirs();
        images.resources().forEach(image ->
                image.copyTo(imagesFolder.file(image.fileName()), UPDATE, ProgressReporter.NULL));
    }

    private String index(final String block, final StringList index, boolean addHtmlAnchors)
    {
        final var matcher = SECTION_HEADING.matcher(block);
        if (matcher.find())
        {
            final var anchored = new StringBuilder();
            do
            {
                final var heading = matcher.group(1).trim();
                final var anchor = heading.toLowerCase().replaceAll(" ", "-");
                index.add("[**" + heading + "**](#" + anchor + ")");
                if (Strings.isEmpty(matcher.group(2)))
                {
                    final var htmlAnchor = addHtmlAnchors ? " <a name = \"" + anchor + "\"></a>" : "";
                    matcher.appendReplacement(anchored, "### " + heading + htmlAnchor) ;
                }
            }
            while (matcher.find());
            matcher.appendTail(anchored);
            return anchored.toString();
        }
        return block;
    }

    private StringList sections(final UmlType type, final Pattern javadocSectionPattern)
    {
        final var javadocUrl = project.property("project-javadoc-url");
        final var qualifiedName = type.name(Name.Qualification.QUALIFIED, Name.TypeParameters.WITHOUT_TYPE_PARAMETERS);
        final var name = type.name(Name.Qualification.UNQUALIFIED, Name.TypeParameters.WITHOUT_TYPE_PARAMETERS);
        final var sections = new StringList();
        final var javadocSections = new HashSet<>(type.documentationSections());
        if (javadocSections.isEmpty())
        {
            final var javadoc = type.type().getJavadoc();
            if (javadoc.isPresent())
            {
                final var text = javadoc.get().toText();
                final var matcher = javadocSectionPattern.matcher(text);
                while (matcher.find())
                {
                    javadocSections.add(matcher.group(1));
                }
            }
            if (javadocSections.isEmpty())
            {
                javadocSections.add("");
            }
        }

        for (final var section : javadocSections)
        {
            if (sections.isEmpty())
            {
                sections.add("| [*" + name + "*](" + javadocUrl + "/" + Name.packageName(qualifiedName).replaceAll("\\.", "/") + "/" + name + ".html) | " + section + " |  ");
            }
            else
            {
                sections.add("| | " + section + " |  ");
            }
        }

        return sections;
    }

    private StringList userTextBlocks(final File readmeFile)
    {
        final var blocks = new StringList();
        if (readmeFile.exists())
        {
            final var readme = readmeFile.reader().string();
            final var matcher = Pattern.compile("(?x) \\[//]: \\s+ \\# \\s+ \\(start-user-text\\) (.+?) \\[//]: \\s+ \\# \\s+ \\(end-user-text\\)", DOTALL).matcher(readme);
            while (matcher.find())
            {
                blocks.add(matcher.group(1).trim());
            }
        }
        return blocks;
    }
}
