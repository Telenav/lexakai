////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.lexakai.indexes;

import com.telenav.kivakit.core.filesystem.File;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.core.kernel.language.strings.Strings;
import com.telenav.kivakit.core.kernel.language.strings.Wrap;
import com.telenav.kivakit.core.kernel.language.time.LocalTime;
import com.telenav.kivakit.core.resource.CopyMode;
import com.telenav.kivakit.core.resource.Resource;
import com.telenav.kivakit.core.resource.resources.packaged.Package;
import com.telenav.kivakit.core.resource.resources.packaged.PackageResource;
import com.telenav.kivakit.core.resource.resources.string.StringResource;
import com.telenav.lexakai.LexakaiProject;
import com.telenav.lexakai.library.Names;
import com.telenav.lexakai.types.UmlType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static com.telenav.kivakit.core.resource.CopyMode.UPDATE;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;

/**
 * Updates the section indexes in the README.md file for a project.
 *
 * <p><b>Usage</b></p>
 *
 * <p>
 * The project is passed to the constructor. The {@link #update(List)} method updates the read me file. The {@link
 * LexakaiProject#javadocSectionPattern()} setting determines how Javadoc sections are located. The {@link
 * LexakaiProject#childProjects()} setting provides a list of projects to index for projects with *pom* packaging
 * (parent projects). Finally, the {@link LexakaiProject#addHtmlAnchors()} determines if anchor tags should be added to
 * the sections that the index references.
 * </p>
 *
 * <p><b>Templates</b></p>
 *
 * <p>
 * The *lexakai-readme-template.md* and *lexakai-parent-readme-template.md* files serve as default templates for
 * producing *README.md* documentation indexes for projects. The former creates *README.md* indexes for ordinary
 * projects with Java source code, while the latter produces *README.md* indexes for Maven parent projects with *pom*
 * packaging. Both templates are copied on the first run into the documentation folder where they can be modified to
 * produce specific effects.
 * </p>
 *
 * <p>
 * After the *README.md* file has been created on the first run, on subsequent runs, it is updated by extracting the
 * portion of the file between *start-user-text* and *end-user-text* comments and inserting it back into the appropriate
 * template.
 * </p>
 *
 * @author jonathanl (shibo)
 */
public class ReadMeIndexUpdater
{
    private static final Resource README_TEMPLATE = PackageResource.packageResource(ReadMeIndexUpdater.class, "lexakai-readme-template.md");

    private static final Resource PARENT__README_TEMPLATE = PackageResource.packageResource(ReadMeIndexUpdater.class, "lexakai-parent-readme-template.md");

    private final LexakaiProject project;

    private final Pattern SECTION_HEADING = Pattern.compile("^### ([ A-Za-z0-9_-]+)(\\s*<a name)?", MULTILINE);

    public ReadMeIndexUpdater(final LexakaiProject project)
    {
        this.project = project;
    }

    public void update(final List<LexakaiProject> childProjects)
    {
        // Get any existing project readme template or create a new one if not exists.
        final var projectReadmeTemplate = project.readmeTemplateFile();
        if (!projectReadmeTemplate.exists())
        {
            README_TEMPLATE.safeCopyTo(projectReadmeTemplate, CopyMode.OVERWRITE, ProgressReporter.NULL);
        }

        // Get any existing parent project readme template or create a new one if not exists.
        final var parentProjectReadmeTemplate = project.parentReadmeTemplateFile();
        if (!parentProjectReadmeTemplate.exists())
        {
            README_TEMPLATE.safeCopyTo(parentProjectReadmeTemplate, CopyMode.OVERWRITE, ProgressReporter.NULL);
        }

        // Get any user text blocks from any existing read me file,
        final var index = new StringList();
        final var blocks = userTextBlocks(project.readmeFile());
        final var topBlock = index(blocks.getOrDefault(0, ""), index, project.addHtmlAnchors());
        final var bottomBlock = index(blocks.getOrDefault(1, ""), index, project.addHtmlAnchors());

        // create a variable map for the readme template,
        final var variables = project.properties();
        variables.put("user-text-top", topBlock);
        variables.put("user-text-bottom", bottomBlock);
        variables.put("project-index", index.join("  \n") + (index.isEmpty() ? "" : "  "));
        variables.put("date", LocalTime.now().asDateString());
        variables.put("time", LocalTime.now().asTimeString());
        if (project.hasSourceCode())
        {
            variables.put("project-javadoc-coverage", project.javadocCoverage().percent().toString());
        }
        else
        {
            variables.put("project-javadoc-coverage", project
                    .childProjects()
                    .filtered(LexakaiProject::hasSourceCode)
                    .join("  \n", project -> "&nbsp; " + project.javadocCoverage().meterMarkdown() + " &nbsp; &nbsp; *" + project.name() + "*"));
        }
        final var undocumented = project.javadocCoverage().significantUndocumentedClasses();
        variables.put("project-undocumented-classes",
                undocumented.isEmpty() ? "" : "The following significant classes are undocumented:  \n\n" +
                        undocumented.prefixedWith("- ").join("  \n"));
        if (!variables.containsKey("project-footer"))
        {
            variables.put("project-footer", "");
        }

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
            sorted.sort(Comparator.comparing(type -> type.name(Names.Qualification.UNQUALIFIED, Names.TypeParameters.WITHOUT_TYPE_PARAMETERS)));
            sorted.forEach(type -> sections.addAll(sections(type, project.javadocSectionPattern())));

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
                childProjects.forEach(at -> childProjectMarkdown.add("[**" + at.name() + "**](" + at.name() + "/README.md)  "));
            }

            // and populate the variable map with this information,
            variables.put("child-projects", childProjectMarkdown.join("\n"));
        }

        // then write the interpolated template,
        final var template = (project.hasSourceCode() ? README_TEMPLATE : PARENT__README_TEMPLATE).reader().string();
        final var expanded = variables.expand(template);
        new StringResource(expanded).safeCopyTo(project.readmeFile(), UPDATE, ProgressReporter.NULL);

        // and finally, update the referenced images.
        final var images = Package.of(getClass(), "documentation/images");
        final var imagesFolder = project.imagesFolder().absolute().mkdirs();
        images.resources().forEach(image ->
                image.copyTo(imagesFolder.file(image.fileName()), UPDATE, ProgressReporter.NULL));
    }

    private String index(final String block, final StringList index, final boolean addHtmlAnchors)
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
                    matcher.appendReplacement(anchored, "### " + heading + htmlAnchor);
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
        final var qualifiedName = type.name(Names.Qualification.QUALIFIED, Names.TypeParameters.WITHOUT_TYPE_PARAMETERS);
        final var name = type.name(Names.Qualification.UNQUALIFIED, Names.TypeParameters.WITHOUT_TYPE_PARAMETERS);
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
                sections.add("| [*" + name + "*](" + javadocUrl + "/" + Names.packageName(qualifiedName).replaceAll("\\.", "/") + "/" + name + ".html) | " + section + " |  ");
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
