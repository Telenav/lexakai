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

package com.telenav.lexakai;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.telenav.kivakit.configuration.BaseComponent;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.primitives.Ints;
import com.telenav.kivakit.kernel.language.values.level.Percent;
import com.telenav.kivakit.kernel.language.values.version.Version;
import com.telenav.kivakit.kernel.messaging.Message;
import com.telenav.kivakit.resource.path.Extension;
import com.telenav.kivakit.resource.resources.other.PropertyMap;
import com.telenav.kivakit.resource.resources.packaged.Package;
import com.telenav.lexakai.indexes.ReadMeUpdater;
import com.telenav.lexakai.javadoc.JavadocCoverage;
import com.telenav.lexakai.library.Diagrams;
import com.telenav.lexakai.library.Names;
import com.telenav.lexakai.types.UmlType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.telenav.kivakit.resource.CopyMode.DO_NOT_OVERWRITE;

/**
 * Represents a project for which Lexakai is producing diagrams.
 *
 * <p><b>Java Parsing</b></p>
 * <p>
 * The project has types that are discovered using the JavaParser API. Those types are available through {@link
 * #typeDeclarations(Consumer)}.
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
public class LexakaiProject extends BaseComponent implements Comparable<LexakaiProject>
{
    /** Parser to use on project source files */
    private final JavaParser parser;

    /** Reference to the application that created this project model */
    private final Lexakai lexakai;

    /** The project version */
    private Version version;

    /** True to include equals, hashCode and toString */
    private boolean includeObjectMethods;

    /** THe set of type declarations in this project */
    private final List<TypeDeclaration<?>> typeDeclarations = new ArrayList<>();

    /** The UML diagrams in this project, deduced from @UmlClassDiagram annotations */
    private final LinkedHashMap<String, LexakaiClassDiagram> diagrams = new LinkedHashMap<>();

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
     * Properties for this project from system properties, project.properties, lexakai.settings and lexakai.properties
     */
    private LexakaiProjectProperties properties;

    /** Locations of project files */
    private final LexakaiProjectFiles files;

    /** Locations of project folders */
    private final LexakaiProjectFolders folders;

    public LexakaiProject(final Lexakai lexakai,
                          final Version version,
                          final Folder root,
                          final Folder project,
                          final Folder outputRoot,
                          final JavaParser parser)
    {
        this.lexakai = lexakai;
        this.version = version;
        this.parser = parser;

        if (version == null)
        {
            final var propertiesFile = root.file("project.properties");
            if (!propertiesFile.exists())
            {
                lexakai.exit("Project.properties file does not exist: $", propertiesFile);
            }
            final var properties = PropertyMap.load(propertiesFile);
            final var rootVersion = properties.get("project-version");
            if (rootVersion == null)
            {
                lexakai.exit("Root project.properties file does not contain a project-version key: $", propertiesFile);
            }
            this.version = Version.parse(rootVersion);
            if (this.version == null)
            {
                lexakai.exit("Project project.properties declares invalid project-version: $", rootVersion);
            }
        }

        folders = new LexakaiProjectFolders(this, root, project, outputRoot);
        files = new LexakaiProjectFiles(this);

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
            children = ObjectList.objectList(folders().project().absolute()
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

    public LexakaiProjectFiles files()
    {
        return files;
    }

    public LexakaiProjectFolders folders()
    {
        return folders;
    }

    public boolean hasSourceCode()
    {
        return folders().sourceCode().exists();
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
        final var resourcePackage = Package.of(Lexakai.class, "resources");

        // If the project has source code,
        if (hasSourceCode())
        {
            // install the lexakai theme and default groups patterns into the configuration folder if they are not already installed,
            final var copyMode = lookup(Lexakai.class).resourceCopyMode();
            resourcePackage.resource("source/lexakai.groups").safeCopyTo(folders().settings(), copyMode);
            resourcePackage.resource("lexakai.theme").safeCopyTo(folders().settings(), copyMode);
        }

        // install the lexakai settings properties file if it doesn't already exist,
        resourcePackage.resource("lexakai.settings")
                .asStringResource()
                .transform(text -> lexakai.properties().expand(text))
                .safeCopyTo(files().lexakaiSettings(), DO_NOT_OVERWRITE);

        // then install the lexakai properties file if it doesn't already exist.
        resourcePackage.resource(hasSourceCode() ? "source/lexakai.properties" : "parent/lexakai.properties")
                .asStringResource()
                .transform(text -> lexakai.properties().expand(text))
                .safeCopyTo(files().lexakaiProperties(), DO_NOT_OVERWRITE);
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

    public Lexakai lexakai()
    {
        return lexakai;
    }

    public String link(final Folder folder)
    {
        return "[**" + name() + "**](" + folder + "/README.md)";
    }

    @NotNull
    public String meterMarkdownForPercent(final Percent percent)
    {
        final var images = properties().imagesLocation();
        final var png = "meter-" + Ints.quantized(percent.asInt(), 10) + "-96";
        return Message.format("<img src=\"$/$.png\" srcset=\"$/$-2x.png 2x\"/>\n", images, png, images, png);
    }

    public String name()
    {
        final var relative = folders().projectRelativeToRoot().path();
        return (relative.isEmpty()
                ? folders().project().name().name()
                : relative.join("-"));
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

    public LexakaiProjectProperties properties()
    {
        if (properties == null)
        {
            properties = new LexakaiProjectProperties(this);
        }
        return properties;
    }

    public String property(final String key)
    {
        final var properties = properties();
        final var value = properties.get(key);
        return value == null ? null : properties.expand(value);
    }

    public String rootProjectName()
    {
        return folders().root().name().name();
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
            folders().sourceCode().nestedFiles(Extension.JAVA.fileMatcher()).forEach(file ->
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
}
