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
import com.telenav.kivakit.component.BaseComponent;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.core.language.collections.list.ObjectList;
import com.telenav.kivakit.language.primitive.Ints;
import com.telenav.kivakit.language.level.Percent;
import com.telenav.kivakit.language.version.Version;
import com.telenav.kivakit.core.messaging.Message;
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
import java.util.Objects;
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
    /** True to add HTML anchors to indexes */
    private boolean addHtmlAnchors;

    /** True to automatically guess method groups */
    private boolean automaticMethodGroups;

    /** True to build a diagram of all public types in each package */
    private boolean buildPackageDiagrams;

    /** Any child projects of this project */
    private ObjectList<LexakaiProject> children;

    /** Javadoc coverage for sub-projects or types in this project */
    private ObjectList<JavadocCoverage> coverage;

    /** The UML diagrams in this project, deduced from @UmlClassDiagram annotations */
    private final LinkedHashMap<String, LexakaiClassDiagram> diagrams = new LinkedHashMap<>();

    /** Locations of project files */
    private final LexakaiProjectFiles files;

    /** Locations of project folders */
    private final LexakaiProjectFolders folders;

    /** True to include equals, hashCode and toString */
    private boolean includeObjectMethods;

    /** True to include protected methods */
    private boolean includeProtectedMethods;

    /** The regular expression pattern for extracting the names of javadoc sections */
    private Pattern javadocSectionPattern;

    /** Reference to the application that created this project model */
    private final Lexakai lexakai;

    /** Parser to use on project source files */
    private final JavaParser parser;

    /**
     * Properties for this project from system properties, project.properties, lexakai.settings and lexakai.properties
     */
    private LexakaiProjectProperties properties;

    /** THe set of type declarations in this project */
    private final List<TypeDeclaration<?>> typeDeclarations = new ArrayList<>();

    /** The project version */
    private Version version;

    protected LexakaiProject(Lexakai lexakai,
                             Version version,
                             Folder root,
                             Folder project,
                             Folder outputRoot,
                             JavaParser parser)
    {
        this.lexakai = lexakai;
        this.version = version;
        this.parser = parser;

        if (version == null)
        {
            var propertiesFile = root.file("project.properties");
            if (!propertiesFile.exists())
            {
                lexakai.exit("Project.properties file does not exist: $", propertiesFile);
            }
            var properties = PropertyMap.load(this, propertiesFile);
            var rootVersion = properties.get("project-version");
            if (rootVersion == null)
            {
                lexakai.exit("Root project.properties file does not contain a project-version key: $", propertiesFile);
            }
            this.version = Version.parse(this, rootVersion);
            if (this.version == null)
            {
                lexakai.exit("Project project.properties declares invalid project-version: $", rootVersion);
            }
        }

        folders = new LexakaiProjectFolders(this, root, project, outputRoot);
        files = new LexakaiProjectFiles(this);
    }

    public LexakaiProject addHtmlAnchors(boolean addHtmlAnchors)
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

    public LexakaiProject automaticMethodGroups(boolean automaticMethodGroups)
    {
        this.automaticMethodGroups = automaticMethodGroups;
        return this;
    }

    public Percent averageProjectJavadocCoverage()
    {
        double total = 0;
        var coverage = nestedProjectJavadocCoverage().uniqued();
        for (var at : coverage)
        {
            total += at.projectCoverage().value();
        }
        return Percent.of(total / coverage.size());
    }

    public LexakaiProject buildPackageDiagrams(boolean packageDiagrams)
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
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet()))
                    .sorted();
        }
        return children;
    }

    @Override
    public int compareTo(@NotNull LexakaiProject that)
    {
        return name().compareTo(that.name());
    }

    /**
     * Calls the consumer with each diagram in this project
     */
    public void diagrams(Consumer<LexakaiClassDiagram> consumer)
    {
        // If we haven't created the diagrams yet,
        if (diagrams.isEmpty())
        {
            // go through each type declaration,
            typeDeclarations(type ->
            {
                if (buildPackageDiagrams)
                {
                    var qualifiedName = type.getFullyQualifiedName().orElse(null);
                    if (qualifiedName != null)
                    {
                        var diagramName = Names.packageName(qualifiedName);
                        var diagram = diagrams.computeIfAbsent(diagramName,
                                ignored -> listenTo(new LexakaiClassDiagram(this, diagramName)));

                        // and include the type
                        diagram.include(new UmlType(diagram, type));
                    }
                }

                // and go through the diagram names the type belongs to,
                for (String diagramName : Diagrams.diagrams(type, buildPackageDiagrams))
                {
                    // get the diagram,
                    var diagram = diagrams.computeIfAbsent(diagramName,
                            ignored -> listenTo(new LexakaiClassDiagram(this, diagramName)));

                    // and include the type
                    diagram.include(new UmlType(diagram, type));
                }
            });
        }

        // Call the consumer with each diagram.
        var sorted = new ArrayList<>(diagrams.values());
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

    public LexakaiProject includeObjectMethods(boolean include)
    {
        includeObjectMethods = include;
        return this;
    }

    public boolean includeProtectedMethods()
    {
        return includeProtectedMethods;
    }

    public LexakaiProject includeProtectedMethods(boolean include)
    {
        includeProtectedMethods = include;
        return this;
    }

    public boolean initialize()
    {
        if (!isValid())
        {
            return false;
        }

        var resourcePackage = Package.packageFrom(this, Lexakai.class, "resources");

        // If the project has source code,
        if (hasSourceCode())
        {
            // install the lexakai theme and default groups patterns into the configuration folder if they are not already installed,
            var copyMode = lookup(Lexakai.class).resourceCopyMode();
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

        return true;
    }

    public boolean isValid()
    {
        return files().lexakaiProperties() != null;
    }

    public Pattern javadocSectionPattern()
    {
        return javadocSectionPattern;
    }

    public LexakaiProject javadocSectionPattern(Pattern pattern)
    {
        javadocSectionPattern = pattern;
        return this;
    }

    public Lexakai lexakai()
    {
        return lexakai;
    }

    public String link(Folder folder)
    {
        return "[**" + name() + "**](" + folder.file("README.md") + ")";
    }

    @NotNull
    public String meterMarkdownForPercent(Percent percent)
    {
        var images = properties().imagesLocation();
        var png = "meter-" + Ints.quantized(percent.asInt(), 10) + "-96";
        return Message.format("<img src=\"$/$.png\" srcset=\"$/$-2x.png 2x\"/>\n", images, png, images, png);
    }

    public String name()
    {
        var relative = folders().projectRelativeToRoot().withoutTrailingSlash().path();
        return (relative.isEmpty()
                ? folders().project().name().name()
                : relative.join("-"));
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
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
                for (var child : childProjects())
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
            properties = LexakaiProjectProperties.load(this);
        }
        return properties;
    }

    public String property(String key)
    {
        var properties = properties();
        var value = properties.get(key);
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
    public void typeDeclarations(Consumer<TypeDeclaration<?>> consumer)
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

    private boolean isProject(Folder folder)
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
                        var parse = parser.parse(file.asJavaFile());

                        // and if that is successful,
                        if (parse.isSuccessful())
                        {
                            // get the result and add the declarations to the set.
                            parse.getResult().ifPresent(unit ->
                                    unit.findAll(TypeDeclaration.class)
                                            .stream()
                                            .filter(type ->
                                            {
                                                var qualifiedName = (Optional<String>) type.getFullyQualifiedName();
                                                return qualifiedName.filter(name -> !name.contains("lexakai.diagrams")).isPresent();
                                            })
                                            .forEach(typeDeclarations::add));
                        }
                        else
                        {
                            problem("Parse not successful: $\n$", file, parse);
                        }
                    }
                    catch (Exception e)
                    {
                        problem(e, "Parse failed with exception: $", file);
                    }
                }
            });

            typeDeclarations.sort(Comparator.comparing(Names::simpleName));
        }
        return typeDeclarations;
    }

    private JavadocCoverage projectJavadocCoverage()
    {
        var coverage = new JavadocCoverage(this);
        typeDeclarations(coverage::add);
        return coverage;
    }
}
