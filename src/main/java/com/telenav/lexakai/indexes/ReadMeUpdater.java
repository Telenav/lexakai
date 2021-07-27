////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.lexakai.indexes;

import com.telenav.kivakit.configuration.BaseComponent;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.collections.map.string.VariableMap;
import com.telenav.kivakit.kernel.language.strings.Strings;
import com.telenav.kivakit.kernel.language.strings.Wrap;
import com.telenav.kivakit.kernel.language.time.LocalTime;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.resource.resources.packaged.PackageResource;
import com.telenav.kivakit.resource.resources.string.StringResource;
import com.telenav.lexakai.Lexakai;
import com.telenav.lexakai.LexakaiProject;
import com.telenav.lexakai.types.UmlType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.regex.Pattern;

import static com.telenav.kivakit.resource.CopyMode.OVERWRITE;
import static com.telenav.lexakai.library.Names.Qualification.UNQUALIFIED;
import static com.telenav.lexakai.library.Names.TypeParameters.WITHOUT_TYPE_PARAMETERS;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;

/**
 * Updates the section indexes in the README.md file for a project.
 *
 * <p><b>Usage</b></p>
 *
 * <p>
 * The project is passed to the constructor. The {@link #update()} method updates the read me file. The {@link
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
public class ReadMeUpdater extends BaseComponent
{
    private static final Resource SOURCE_README_TEMPLATE = PackageResource.of(ReadMeUpdater.class, "lexakai-source-readme-template.md");

    private static final Resource PARENT_README_TEMPLATE = PackageResource.of(ReadMeUpdater.class, "lexakai-parent-readme-template.md");

    private final LexakaiProject project;

    private final Pattern SECTION_HEADING = Pattern.compile("^### ([ A-Za-z0-9_-]+)(\\s*<a name)?", MULTILINE);

    public ReadMeUpdater(final LexakaiProject project)
    {
        this.project = project;
    }

    /**
     * Updates the README.md for this project
     */
    public void update()
    {
        // Get any user text blocks from any existing read me file,
        final var index = new StringList();
        final var blocks = userTextBlocks(project.files().readme());
        final var topBlock = indexUserText(blocks.getOrDefault(0, ""), index, project.addHtmlAnchors());
        final var middleBlock = indexUserText(blocks.getOrDefault(1, ""), index, project.addHtmlAnchors());
        final var bottomBlock = indexUserText(blocks.getOrDefault(2, ""), index, project.addHtmlAnchors());

        // create a variable map for the readme template,
        final var properties = project.properties();
        properties.put("project-javadoc-average-coverage", project.averageProjectJavadocCoverage().toString());
        properties.put("project-javadoc-average-coverage-meter", project.meterMarkdownForPercent(project.averageProjectJavadocCoverage()));
        properties.put("project-index", index.join("  \n") + (index.isEmpty() ? "" : "  "));
        properties.put("date", LocalTime.now().asDateString());
        properties.put("time", LocalTime.now().asTimeString());

        // and if the project has source code,
        if (project.hasSourceCode())
        {
            // add the appropriate variables,
            addProjectVariables(properties);
        }
        else
        {
            // or the variables for a parent project,
            addParentProjectVariables(properties);
        }

        // expand variables in the user blocks,
        properties.put("user-text-top", expand(properties, topBlock));
        properties.put("user-text-middle", expand(properties, middleBlock));
        properties.put("user-text-bottom", expand(properties, bottomBlock));

        // then write the interpolated template
        final var template = readMeTemplate().reader().string();
        final var expanded = expand(properties, template);

        // to the readme file in the source tree and the readme file in the output tree.
        final var readme = new StringResource(expanded);
        readme.safeCopyTo(project.files().readme(), OVERWRITE);
    }

    /**
     * Adds variables relevant to a project with child projects
     *
     * @param variables The variable map to populate
     */
    private void addParentProjectVariables(final VariableMap<String> variables)
    {
        variables.put("project-javadoc-coverage", project
                .nestedProjectJavadocCoverage()
                .join("  \n", coverage ->
                {
                    final var child = coverage.project().folders().project();
                    final var projectFolder = project.folders().project();
                    final var folder = child.relativeTo(projectFolder);
                    return coverage.projectCoverageMeter(folder);
                }));

        final var childProjectMarkdown = new StringList();
        final var childProjects = project.childProjects();
        if (!childProjects.isEmpty())
        {
            childProjects.forEach(at ->
            {
                final var child = at.folders().project().last();
                childProjectMarkdown.add(at.link(child) + "  ");
            });
        }

        // and populate the variable map with this information,
        variables.put("child-projects", childProjectMarkdown.join("\n"));
    }

    /**
     * Adds variables relevant to a project with source code
     *
     * @param variables The variable map to populate
     */
    private void addProjectVariables(final VariableMap<String> variables)
    {
        // Add diagram links,
        final var types = new HashSet<UmlType>();
        final var classDiagramIndex = new StringList();
        final var packageDiagramIndex = new StringList();
        project.diagrams(diagram ->
        {
            if (!Strings.isEmpty(diagram.title()))
            {
                final var line = "[*" + diagram.title() + "*](" + project.properties().outputDiagramsLocation() + "/" + diagram.identifier() + ".svg)";
                (diagram.isPackageDiagram() ? packageDiagramIndex : classDiagramIndex).add(line);
                types.addAll(diagram.includedQualifiedTypes());
            }
        });
        if (classDiagramIndex.isEmpty())
        {
            classDiagramIndex.add("None");
        }
        if (packageDiagramIndex.isEmpty())
        {
            packageDiagramIndex.add("None");
        }
        variables.put("class-diagram-index", classDiagramIndex.join("  \n"));
        variables.put("package-diagram-index", packageDiagramIndex.join("  \n"));

        // add Javadoc coverage information,
        project.nestedProjectJavadocCoverage().first().addToVariableMap(variables);

        // and javadoc sections,
        final var sections = new StringList();
        final var sorted = new ArrayList<>(types);
        sorted.sort(Comparator.comparing(type -> type.name(UNQUALIFIED, WITHOUT_TYPE_PARAMETERS)));
        sorted.forEach(type -> sections.addAll(javadocSections(type, project.javadocSectionPattern())));
        variables.put("javadoc-index", sections.join("\n"));

        // and the wrapped project description.
        final var description = variables.get("project-description");
        if (description.length() > 120)
        {
            final var wrapped = Wrap.wrap(description, 100).replaceAll("\n", "  \n");
            variables.put("project-description", wrapped);
        }
    }

    /**
     * Expands the given text with the variable map, but retains ${x} markers in comments.
     *
     * @param variables The variables to substitute
     * @param text The text to expand
     * @return The expanded text
     */
    private String expand(final VariableMap<String> variables, final String text)
    {
        // Replace "<!-- ${x} --> .* <!-- end -->" with "<!-- <<<x>>> --> ${x} <!-- end -->"
        final var transformed = text.replaceAll("<!-- \\$\\{(.*?)} -->.*?<!-- end -->", "<!-- <<<$1>>> --> \\$\\{$1} <!-- end -->");

        // expand the transformed string, producing "<<<x>>> <expanded> <<<end>>>"
        final var expanded = variables.expand(transformed, "");

        // and finally turn the expanded string into "<!-- ${x} --> <expanded> <!-- end -->"
        return expanded.replaceAll("<<<(.*?)>>>", "\\$\\{$1}");
    }

    /**
     * @param index The section index to populate with references
     * @return The given block of user text with HTML anchors added
     */
    private String indexUserText(final String block, final StringList index, final boolean addHtmlAnchors)
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

    /**
     * @return The Javadoc sections matching the given pattern for the given type
     */
    private StringList javadocSections(final UmlType type, final Pattern javadocSectionPattern)
    {
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

        final var name = type.name(UNQUALIFIED, WITHOUT_TYPE_PARAMETERS);

        for (final var section : javadocSections)
        {
            if (sections.isEmpty())
            {
                sections.add("| [*" + name + "*](" + project.properties().outputJavadocLocation(type) + ") | " + section + " |  ");
            }
            else
            {
                sections.add("| | " + section + " |  ");
            }
        }

        return sections;
    }

    /**
     * @return The readme template for a project with source code
     */
    private File readMeTemplate()
    {
        final var template = project.files().readMeTemplate();
        (project.hasSourceCode() ? SOURCE_README_TEMPLATE : PARENT_README_TEMPLATE)
                .safeCopyTo(template, lookup(Lexakai.class).resourceCopyMode());
        return template;
    }

    /**
     * @return All user text blocks in the given file
     */
    private StringList userTextBlocks(final File file)
    {
        final var blocks = new StringList();
        if (file.exists())
        {
            final var readme = file.reader().string();
            final var matcher = Pattern.compile("(?x) \\[//]: \\s+ \\# \\s+ \\(start-user-text\\) (.+?) \\[//]: \\s+ \\# \\s+ \\(end-user-text\\)", DOTALL).matcher(readme);
            while (matcher.find())
            {
                blocks.add(matcher.group(1).trim());
            }
        }
        return blocks;
    }
}
