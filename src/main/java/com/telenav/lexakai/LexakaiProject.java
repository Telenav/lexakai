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

package com.telenav.lexakai;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.telenav.kivakit.core.filesystem.File;
import com.telenav.kivakit.core.filesystem.Folder;
import com.telenav.kivakit.core.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.core.kernel.language.paths.StringPath;
import com.telenav.kivakit.core.kernel.language.primitives.Ints;
import com.telenav.kivakit.core.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import com.telenav.kivakit.core.kernel.language.values.version.Version;
import com.telenav.kivakit.core.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.resource.path.Extension;
import com.telenav.kivakit.core.resource.resources.other.PropertyMap;
import com.telenav.kivakit.core.resource.resources.packaged.Package;
import com.telenav.lexakai.indexes.ReadMeUpdater;
import com.telenav.lexakai.javadoc.JavadocCoverage;
import com.telenav.lexakai.library.Diagrams;
import com.telenav.lexakai.library.Names;
import com.telenav.lexakai.types.UmlType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.resource.CopyMode.DO_NOT_OVERWRITE;

/**
 * Represents a project for which Lexakai is producing diagrams.
 *
 * <p><b>Java Parsing</b></p>
 * <p>
 * The project has types that are discovered using the JavaParser API. Those types are available through {@link
 * #typeDeclarations()} and {@link #typeDeclarations(Consumer)}.
 * </p>
 *
 * <p><b>Settings</b></p>
 *
 * <p>
 * Projects have various settings that are used to define how the UML for a project is generated. These settings are
 * configured by the {@link Lexakai} application from the command line.
 * </p>
 *
 * <ul>
 *     <li>{@link #addHtmlAnchors(boolean)}</li>
 *     <li>{@link #automaticMethodGroups(boolean)}</li>
 *     <li>{@link #buildPackageDiagrams(boolean)}</li>
 *     <li>{@link #includeObjectMethods(boolean)}</li>
 *     <li>{@link #includeProtectedMethods(boolean)}</li>
 *     <li>{@link #javadocSectionPattern(Pattern)}</li>
 * </ul>
 *
 * <p><b>Functions</b></p>
 *
 * <p>
 * These methods produce user-facing results for the {@link Lexakai} application:
 * </p>
 *
 * <ul>
 *     <li>{@link #diagrams(Consumer)} - Produces UML diagram(s) for the project</li>
 *     <li>{@link #nestedProjectJavadocCoverage()} - Determines Javadoc coverage for types in the project</li>
 *     <li>{@link #updateReadMe()} - Updates the indexing in README.md for the project</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 */
public class LexakaiProject extends BaseRepeater implements Comparable<LexakaiProject>
{
    /** The folder for this project */
    private final Folder projectFolder;

    /** The root output folder */
    private final Folder outputRootFolder;

    /** Parser to use on project source files */
    private final JavaParser parser;

    /** Reference to the application that created this project model */
    private final Lexakai lexakai;

    /** The project version */
    private final Version version;

    /** The project root folder */
    private final Folder root;

    /** True to include equals, hashCode and toString */
    private boolean includeObjectMethods;

    /** THe set of type declarations in this project */
    private final List<TypeDeclaration<?>> typeDeclarations = new ArrayList<>();

    /** The UML diagrams in this project, deduced from @UmlClassDiagram annotations */
    private final LinkedHashMap<String, LexakaiClassDiagram> diagrams = new LinkedHashMap<>();

    /** The names of all the diagrams in this project */
    private final Set<String> diagramNames = new HashSet<>();

    /** True to automatically guess method groups */
    private boolean automaticMethodGroups;

    /** The regular expression pattern for extracting the names of javadoc sections */
    private Pattern javadocSectionPattern;

    /** True to include protected methods */
    private boolean includeProtectedMethods;

    /** True to add HTML anchors to indexes */
    private boolean addHtmlAnchors;

    /** True to build a diagram of all public types in each package */
    private boolean buildPackageDiagrams;

    /** Javadoc coverage for sub-projects or types in this project */
    private ObjectList<JavadocCoverage> coverage;

    /** Any child projects of this project */
    private ObjectList<LexakaiProject> children;

    /**
     * Properties for this project from system properties, project.properties, lexakai-settings.properties and
     * lexakai.properties
     */
    private PropertyMap properties;

    public LexakaiProject(final Lexakai lexakai,
                          final Version version,
                          final Folder root,
                          final Folder projectFolder,
                          final Folder outputRootFolder,
                          final JavaParser parser)
    {
        this.lexakai = lexakai;
        this.version = version;
        this.root = root;
        this.projectFolder = projectFolder;
        this.outputRootFolder = outputRootFolder;
        this.parser = parser;

        initialize();
    }

    public LexakaiProject addHtmlAnchors(final boolean addHtmlAnchors)
    {
        this.addHtmlAnchors = addHtmlAnchors;
        return this;
    }

    public boolean addHtmlAnchors()
    {
        return addHtmlAnchors;
    }

    public boolean automaticMethodGroups()
    {
        return automaticMethodGroups;
    }

    public LexakaiProject automaticMethodGroups(final boolean automaticMethodGroups)
    {
        this.automaticMethodGroups = automaticMethodGroups;
        return this;
    }

    public Percent averageProjectJavadocCoverage()
    {
        double total = 0;
        final var coverage = nestedProjectJavadocCoverage().uniqued();
        for (final var at : coverage)
        {
            total += at.projectCoverage().value();
        }
        return Percent.of(total / coverage.size());
    }

    public LexakaiProject buildPackageDiagrams(final boolean packageDiagrams)
    {
        buildPackageDiagrams = packageDiagrams;
        return this;
    }

    public boolean buildPackageDiagrams()
    {
        return buildPackageDiagrams;
    }

    public ObjectList<LexakaiProject> childProjects()
    {
        if (children == null)
        {
            children = ObjectList.objectList(projectFolder.absolute()
                    .folders()
                    .stream()
                    .filter(this::isProject)
                    .map(lexakai::project)
                    .collect(Collectors.toSet()))
                    .sorted();
        }
        return children;
    }

    @Override
    public int compareTo(@NotNull final LexakaiProject that)
    {
        return name().compareTo(that.name());
    }

    public Folder diagramFolder()
    {
        return documentationFolder().folder("diagrams");
    }

    public String diagramLocation()
    {
        return StringPath.stringPath(documentationLocation(), relativeFolder().toString(), "diagrams").toString();
    }

    /**
     * Calls the consumer with the names of all diagrams in this project
     */
    public void diagramNames(final Consumer<String> consumer)
    {
        // If we haven't found diagrams in this project,
        if (diagramNames.isEmpty())
        {
            // go through each type declaration,
            typeDeclarations((type) ->
            {
                // and add the diagrams it belongs to.
                diagramNames.addAll(Diagrams.diagrams(type, buildPackageDiagrams));
            });
        }

        diagramNames.forEach(consumer);
    }

    /**
     * Calls the consumer with each diagram in this project
     */
    public void diagrams(final Consumer<LexakaiClassDiagram> consumer)
    {
        // If we haven't created the diagrams yet,
        if (diagrams.isEmpty())
        {
            // go through each type declaration,
            typeDeclarations(type ->
            {
                if (buildPackageDiagrams)
                {
                    final var qualifiedName = type.getFullyQualifiedName().orElse(null);
                    if (qualifiedName != null)
                    {
                        final var diagramName = Names.packageName(qualifiedName);
                        final var diagram = diagrams.computeIfAbsent(diagramName,
                                ignored -> listenTo(new LexakaiClassDiagram(this, diagramName)));

                        // and include the type
                        diagram.include(new UmlType(diagram, type));
                    }
                }

                // and go through the diagram names the type belongs to,
                for (final String diagramName : Diagrams.diagrams(type, buildPackageDiagrams))
                {
                    // get the diagram,
                    final var diagram = diagrams.computeIfAbsent(diagramName,
                            ignored -> listenTo(new LexakaiClassDiagram(this, diagramName)));

                    // and include the type
                    diagram.include(new UmlType(diagram, type));
                }
            });
        }

        // Call the consumer with each diagram.
        final var sorted = new ArrayList<>(diagrams.values());
        sorted.sort(Comparator.comparing(LexakaiClassDiagram::title));
        sorted.forEach(consumer);
    }

    public Folder documentationFolder()
    {
        return outputFolder().folder("documentation").mkdirs();
    }

    public Folder documentationLexakaiFolder()
    {
        return documentationFolder().folder("lexakai").mkdirs();
    }

    public String documentationLocation()
    {
        return properties().asPath("lexakai-documentation-location");
    }

    public Folder folder()
    {
        return projectFolder;
    }

    public boolean hasChildProjects()
    {
        return !childProjects().isEmpty();
    }

    public boolean hasSourceCode()
    {
        return projectFolder.folder("src").exists();
    }

    public Folder imagesFolder()
    {
        return properties().asFolder("lexakai-images-folder");
    }

    public String imagesLocation()
    {
        return properties().asPath("lexakai-images-location");
    }

    /**
     * @return True if equals, hashCode and toString should be included
     */
    public boolean includeObjectMethods()
    {
        return includeObjectMethods;
    }

    public LexakaiProject includeObjectMethods(final boolean include)
    {
        includeObjectMethods = include;
        return this;
    }

    public boolean includeProtectedMethods()
    {
        return includeProtectedMethods;
    }

    public LexakaiProject includeProtectedMethods(final boolean include)
    {
        includeProtectedMethods = include;
        return this;
    }

    public void initialize()
    {
        final var resourceFolder = Package.of(Lexakai.class, "resources");

        // If the project has source code,
        if (hasSourceCode())
        {
            // ensure that diagram folder exists,
            diagramFolder().mkdirs();

            // and install the lexakai theme and default groups patterns if they are not already installed,
            final var copyMode = Lexakai.get().resourceCopyMode();
            resourceFolder.resource("source/lexakai.groups").safeCopyTo(documentationLexakaiFolder(), copyMode, ProgressReporter.NULL);
            resourceFolder.resource("lexakai.theme").safeCopyTo(documentationLexakaiFolder(), copyMode, ProgressReporter.NULL);
        }

        // then install the lexakai properties file if it doesn't already exist,
        resourceFolder.resource(hasSourceCode() ? "source/lexakai.properties" : "parent/lexakai.properties")
                .asStringResource()
                .transform(text -> properties().expand(text))
                .safeCopyTo(propertiesFile(), DO_NOT_OVERWRITE, ProgressReporter.NULL);

        // and install the lexakai settings properties file if it doesn't already exist.
        resourceFolder.resource("lexakai-settings.properties")
                .asStringResource()
                .transform(text -> properties().expand(text))
                .safeCopyTo(outputRootFolder.file("lexakai-settings.properties"), DO_NOT_OVERWRITE, ProgressReporter.NULL);
    }

    public String javadocLocation()
    {
        return properties().asPath("lexakai-javadoc-location");
    }

    public Pattern javadocSectionPattern()
    {
        return javadocSectionPattern;
    }

    public LexakaiProject javadocSectionPattern(final Pattern pattern)
    {
        javadocSectionPattern = pattern;
        return this;
    }

    public String link()
    {
        return "[**" + name() + "**](" + folder().name() + "/README.md)";
    }

    @NotNull
    public String meterMarkdownForPercent(final Percent percent)
    {
        return " ![](" + imagesLocation() + "/meter-" + Ints.quantized(percent.asInt(), 10) + "-12.png)";
    }

    public String name()
    {
        final var parentProject = projectFolder.relativePath(root.absolute());
        return (parentProject.isEmpty()
                ? projectFolder.name().name()
                : parentProject.join("-"));
    }

    public ObjectList<JavadocCoverage> nestedProjectJavadocCoverage()
    {
        if (coverage == null)
        {
            if (hasSourceCode())
            {
                coverage = ObjectList.objectList(projectJavadocCoverage());
            }
            else
            {
                coverage = new ObjectList<>();
                for (final var child : childProjects())
                {
                    coverage.addAll(child.nestedProjectJavadocCoverage());
                }
            }
        }
        return coverage;
    }

    public Folder outputFolder()
    {
        return outputRootFolder().folder(relativeFolder());
    }

    public Folder outputRootFolder()
    {
        return outputRootFolder;
    }

    public File parentReadMeTemplateFile()
    {
        return documentationLexakaiFolder().file("lexakai-parent-readme-template.md");
    }

    public PropertyMap properties()
    {
        if (properties == null)
        {
            properties = PropertyMap.of(lexakai.properties());

            // Add lexakai settings,
            properties.addAll(PropertyMap.load(lexakaiSettingsFile()));

            // project properties (which can override lexakai settings when needed),
            properties.addAll(PropertyMap.load(projectPropertiesFile()));

            // Lexakai properties,
            properties.addAll(PropertyMap.load(propertiesFile()));

            // and project folders.
            properties.add("project-folder", folder().toString());
            properties.add("project-relative-folder", relativeFolder().toString());
            properties.add("project-output-folder", outputFolder().toString());
            properties.add("project-output-root-folder", outputRootFolder().toString());
            properties.add("project-relative-output-folder", relativeOutputFolder().toString());
            properties.add("project-diagram-location", diagramLocation());

            // Ensure that required properties are defined.
            require(properties, "lexakai-documentation-location");
            require(properties, "lexakai-images-location");
            require(properties, "project-name");
            require(properties, "project-description");

            // Add defaults:

            // project-dotted-name: The maven project name with dots instead of dashes (like kivakit.core.application)
            properties.putIfAbsent("project-dotted-name", properties.get("project-name").replaceAll("-", "."));

            // project-icon: Use the gears icon if no icon has been specified
            properties.putIfAbsent("project-icon", properties.asPath("lexakai-images-location") + "/gears-40.png");

            // project-javadoc-location: Compose the javadoc location from the lexakai-javadoc-location and the
            // project's dotted name. For example, https://www.kivakit.org/javadoc/kivakit.core.application
            properties.putIfAbsent("project-javadoc-location", properties.asPath("lexakai-javadoc-location") + "/" + properties.get("project-dotted-name"));
        }
        return properties;
    }

    public String property(final String key)
    {
        final var properties = properties();
        final var value = properties.get(key);
        return value == null ? null : properties.expand(value);
    }

    public File readMeTemplateFile()
    {
        return documentationLexakaiFolder().file("lexakai-readme-template.md");
    }

    public File readmeFile()
    {
        return projectFolder.file("README.md");
    }

    public Folder relativeFolder()
    {
        return folder().relativeTo(root);
    }

    public Folder relativeOutputFolder()
    {
        return outputFolder().relativeTo(root);
    }

    public Folder sourceFolder()
    {
        return projectFolder.folder("src/main/java");
    }

    @Override
    public String toString()
    {
        return name() + " " + version();
    }

    /**
     * Calls the consumer with the type declarations in this project
     */
    public void typeDeclarations(final Consumer<TypeDeclaration<?>> consumer)
    {
        parseTypeDeclarations().forEach(consumer);
    }

    public List<TypeDeclaration<?>> typeDeclarations()
    {
        return typeDeclarations;
    }

    public void updateReadMe()
    {
        new ReadMeUpdater(this).update();
    }

    public Version version()
    {
        return version;
    }

    private boolean isProject(final Folder folder)
    {
        return folder.file("pom.xml").exists() || folder.file("gradle.properties").exists();
    }

    private File lexakaiSettingsFile()
    {
        return outputRootFolder().file("lexakai-settings.properties");
    }

    /**
     * Parse the class, interface and enum declarations under this project's source folder
     */
    @SuppressWarnings("unchecked")
    private List<TypeDeclaration<?>> parseTypeDeclarations()
    {
        // If we have not yet parsed the source code,
        if (typeDeclarations.isEmpty())
        {
            // go through each Java file under the root's source folder,
            sourceFolder().nestedFiles(Extension.JAVA.fileMatcher()).forEach(file ->
            {
                // except for this weird file :),
                if (!file.fileName().name().equals("module-info.java"))
                {
                    try
                    {
                        // parse the file,
                        final var optionalUnit = parser.parse(file.asJavaFile());

                        // and if that is successful,
                        if (optionalUnit.isSuccessful())
                        {
                            // get the result and add the declarations to the set.
                            optionalUnit.getResult().ifPresent(unit ->
                                    unit.findAll(TypeDeclaration.class)
                                            .stream()
                                            .filter(type ->
                                            {
                                                final var qualifiedName = (Optional<String>) type.getFullyQualifiedName();
                                                return qualifiedName.filter(name -> !name.contains("lexakai.diagrams")).isPresent();
                                            })
                                            .forEach(typeDeclarations::add));
                        }
                        else
                        {
                            problem("Unable to parse: $", file);
                        }
                    }
                    catch (final Exception e)
                    {
                        problem(e, "Unable to parse $", file);
                    }
                }
            });

            typeDeclarations.sort(Comparator.comparing(Names::simpleName));
        }
        return typeDeclarations;
    }

    private JavadocCoverage projectJavadocCoverage()
    {
        final var coverage = new JavadocCoverage(this);
        typeDeclarations(coverage::add);
        return coverage;
    }

    private File projectPropertiesFile()
    {
        return sourceFolder().file("project.properties");
    }

    private File propertiesFile()
    {
        return documentationLexakaiFolder().file("lexakai.properties");
    }

    private void require(final PropertyMap map, final String key)
    {
        final var value = map.get(key);
        ensure(value != null && !value.contains("[UNDEFINED]"), "The key '$' is not defined in lexakai-settings.properties or lexakai.properties", key);
    }
}
