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
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.collections.map.string.VariableMap;
import com.telenav.kivakit.core.kernel.language.paths.PackagePath;
import com.telenav.kivakit.core.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.core.kernel.language.strings.Strip;
import com.telenav.kivakit.core.kernel.language.values.count.MutableCount;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import com.telenav.kivakit.core.kernel.language.values.version.Version;
import com.telenav.kivakit.core.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.resource.path.Extension;
import com.telenav.kivakit.core.resource.resources.other.PropertyMap;
import com.telenav.kivakit.core.resource.resources.packaged.Package;
import com.telenav.lexakai.indexes.ReadMeIndexUpdater;
import com.telenav.lexakai.library.Diagrams;
import com.telenav.lexakai.library.Names;
import com.telenav.lexakai.types.UmlType;

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
 *     <li>{@link #javadocCoverage(int)} - Determines Javadoc coverage for types in the project</li>
 *     <li>{@link #updateReadMe()} ()} - Updates the indexing in README.md for the project</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 */
public class LexakaiProject extends BaseRepeater
{
    private final Folder projectFolder;

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

    public LexakaiProject(final Lexakai lexakai,
                          final Version version,
                          final Folder root,
                          final Folder projectFolder,
                          final JavaParser parser)
    {
        this.lexakai = lexakai;
        this.version = version;
        this.root = root;
        this.projectFolder = projectFolder;
        this.parser = parser;

        initialize();
    }

    public LexakaiProject addHtmlAnchors(final boolean addHtmlAnchors)
    {
        this.addHtmlAnchors = addHtmlAnchors;
        return this;
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

    public LexakaiProject buildPackageDiagrams(final boolean packageDiagrams)
    {
        buildPackageDiagrams = packageDiagrams;
        return this;
    }

    public boolean buildPackageDiagrams()
    {
        return buildPackageDiagrams;
    }

    public List<Folder> childProjects()
    {
        return ObjectList.objectList(projectFolder.absolute()
                .folders()
                .stream()
                .filter(this::isProject)
                .collect(Collectors.toSet()))
                .sorted();
    }

    public Folder diagramFolder()
    {
        return documentationFolder().folder("diagrams");
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
        return projectFolder.folder("documentation").mkdirs();
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
        return documentationFolder().folder("images");
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
        final var resourceFolder = Package.of(PackagePath.parsePackagePath(Lexakai.class,
                hasSourceCode() ? "resources/source" : "resources/parent"));

        // If the project has source code,
        if (hasSourceCode())
        {
            // ensure that diagram folder exists,
            diagramFolder().mkdirs();

            // and install the lexakai theme and default groups patterns if they are not already installed,
            resourceFolder.resource("lexakai.groups").safeCopyTo(documentationFolder(), DO_NOT_OVERWRITE, ProgressReporter.NULL);
            resourceFolder.resource("lexakai.theme").safeCopyTo(documentationFolder(), DO_NOT_OVERWRITE, ProgressReporter.NULL);
        }

        // then install the lexakai properties file if it doesn't already exist.
        resourceFolder.resource("lexakai.properties")
                .asStringResource()
                .transform(text -> properties().expand(text))
                .safeCopyTo(propertiesFile(), DO_NOT_OVERWRITE, ProgressReporter.NULL);
    }

    public StringList javadocCoverage(final int minimumLength)
    {
        final var warnings = new StringList();
        final var types = new MutableCount();
        final var covered = new MutableCount();
        typeDeclarations(type ->
        {
            types.increment();
            var requiredLength = minimumLength;
            final var javadoc = type.getJavadoc();
            final var significant = type.toString().length() > 4096 ? "=>  " : "    ";
            if (javadoc.isPresent())
            {
                if (type.isEnumDeclaration())
                {
                    requiredLength = 64;
                }
                final var text = javadoc.get().toText();
                if (text.length() < requiredLength)
                {
                    if (type.getFullyQualifiedName().isPresent())
                    {
                        warnings.add("${string}$: Javadoc is only $ characters (minimum is $)",
                                significant, Strip.packagePrefix(type.getFullyQualifiedName().get()),
                                text.length(), requiredLength);
                    }
                }
                else
                {
                    covered.increment();
                }
            }
            else
            {
                if (type.getFullyQualifiedName().isPresent())
                {
                    warnings.add("${string}$: Javadoc is missing",
                            significant, Strip.packagePrefix(type.getFullyQualifiedName().get()));
                }
            }
        });

        final var coverage = new StringList();
        final var percent = Percent.percent(100.0 * covered.get() / types.get());
        coverage.add("Javadoc coverage for $ is $", name(), percent);
        coverage.addAll(warnings);
        return coverage;
    }

    public LexakaiProject javadocSectionPattern(final Pattern pattern)
    {
        javadocSectionPattern = pattern;
        return this;
    }

    public String name()
    {
        final var parentProject = projectFolder.relativePath(root.absolute());
        return (parentProject.isEmpty()
                ? projectFolder.name().name()
                : parentProject.join("-"));
    }

    public File parentProjectReadmeTemplateFile()
    {
        return documentationFolder().file("parent-project-readme-template.md");
    }

    public File projectReadmeTemplateFile()
    {
        return documentationFolder().file("project-readme-template.md");
    }

    public VariableMap<String> properties()
    {
        final var properties = lexakai.properties().copy();
        properties.addAll(PropertyMap.load(sourceFolder().file("project.properties")));
        properties.addAll(PropertyMap.load(propertiesFile()));
        properties.putIfAbsent("project-icon", "documentation/images/gears-40.png");
        return properties;
    }

    public String property(final String key)
    {
        final var properties = properties();
        final var value = properties.get(key);
        return value == null ? null : properties.expand(value);
    }

    public File readmeFile()
    {
        return projectFolder.file("README.md");
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
        new ReadMeIndexUpdater(this).update(javadocSectionPattern, childProjects(), addHtmlAnchors);
    }

    public Version version()
    {
        return version;
    }

    private boolean isProject(final Folder folder)
    {
        return folder.file("pom.xml").exists() || folder.file("gradle.properties").exists();
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
            sourceFolder().nestedFiles(Extension.JAVA.matcher()).forEach(file ->
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

    private File propertiesFile()
    {
        return documentationFolder().file("lexakai.properties");
    }
}
