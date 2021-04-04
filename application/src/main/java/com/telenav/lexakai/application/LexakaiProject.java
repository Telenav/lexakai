package com.telenav.lexakai.application;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.telenav.kivakit.core.filesystem.File;
import com.telenav.kivakit.core.filesystem.Folder;
import com.telenav.kivakit.core.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.core.kernel.language.collections.map.string.VariableMap;
import com.telenav.kivakit.core.kernel.language.paths.PackagePath;
import com.telenav.kivakit.core.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.core.kernel.language.values.version.Version;
import com.telenav.kivakit.core.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.resource.path.Extension;
import com.telenav.kivakit.core.resource.resources.other.PropertyMap;
import com.telenav.kivakit.core.resource.resources.packaged.Package;
import com.telenav.lexakai.application.indexes.ReadMeIndexUpdater;
import com.telenav.lexakai.application.library.Diagrams;
import com.telenav.lexakai.application.library.Name;
import com.telenav.lexakai.application.types.UmlType;

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
 * @author jonathanl (shibo)
 */
public class LexakaiProject extends BaseRepeater
{
    private final Folder projectFolder;

    /** Parser to use on project source files */
    private final JavaParser parser;

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

    public LexakaiProject(final Lexakai lexakai, final Version version, final Folder root, final Folder projectFolder,
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
                        final var diagramName = Name.packageName(qualifiedName);
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

    public LexakaiProject includeProtectedMethods(final Boolean include)
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
                .transform(text -> properties().expanded(text))
                .safeCopyTo(propertiesFile(), DO_NOT_OVERWRITE, ProgressReporter.NULL);
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
        return value == null ? null : properties.expanded(value);
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
        typeDeclarations().forEach(consumer);
    }

    public List<TypeDeclaration<?>> types()
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

    private File propertiesFile()
    {
        return documentationFolder().file("lexakai.properties");
    }

    /**
     * Parse the class, interface and enum declarations under this project's source folder
     */
    @SuppressWarnings("unchecked")
    private List<TypeDeclaration<?>> typeDeclarations()
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
                                                if (qualifiedName.isPresent())
                                                {
                                                    return !qualifiedName.get().contains("lexakai.diagrams");
                                                }
                                                return false;
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

            typeDeclarations.sort(Comparator.comparing(Name::simpleName));
        }
        return typeDeclarations;
    }
}
