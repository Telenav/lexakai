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
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.telenav.kivakit.application.Application;
import com.telenav.kivakit.commandline.ArgumentParser;
import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.collections.set.ObjectSet;
import com.telenav.kivakit.core.os.Processes;
import com.telenav.kivakit.core.string.AsciiArt;
import com.telenav.kivakit.core.string.IndentingStringBuilder;
import com.telenav.kivakit.core.value.count.MutableCount;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.filesystem.Folder.Traversal;
import com.telenav.kivakit.launcher.JarLauncher;
import com.telenav.kivakit.resource.CopyMode;
import com.telenav.kivakit.resource.packages.PackageResource;
import com.telenav.lexakai.dependencies.DependencyDiagram;
import com.telenav.lexakai.dependencies.MavenDependencyTreeBuilder;
import com.telenav.lexakai.javadoc.JavadocCoverage;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.telenav.kivakit.commandline.SwitchParsers.booleanSwitchParser;
import static com.telenav.kivakit.commandline.SwitchParsers.enumSwitchParser;
import static com.telenav.kivakit.commandline.SwitchParsers.integerSwitchParser;
import static com.telenav.kivakit.commandline.SwitchParsers.stringSwitchParser;
import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.kivakit.filesystem.Folder.folderArgumentParser;
import static com.telenav.kivakit.filesystem.Folder.folderSwitchParser;
import static com.telenav.kivakit.launcher.JarLauncher.ProcessType.CHILD;
import static com.telenav.kivakit.launcher.JarLauncher.RedirectTo.CONSOLE;
import static com.telenav.kivakit.resource.CopyMode.DO_NOT_OVERWRITE;
import static com.telenav.kivakit.resource.CopyMode.UPDATE;

/**
 * The <a href="https://telenav.github.io/lexakai/">Lexakai</a> application.
 * <p>
 * Lexakai creates markdown indexes and UML for the set of project(s) under the given root folder(s). See command line
 * help (pass no arguments) for full details or look at the <a href="https://telenav.github.io/lexakai/">online
 * version</a>.
 * </p>
 *
 * @author jonathanl (shibo)
 * @see <a href="https://telenav.github.io/lexakai/">Lexakai documentation</a>
 */
@SuppressWarnings({ "SpellCheckingInspection", "BooleanMethodIsAlwaysInverted" })
public class Lexakai extends Application
{
    @SuppressWarnings("unused")
    public static String embeddedMain(String[] arguments)
    {
        var result = new Lexakai().run(arguments);
        return result.failed() ? result.messages().join("\n") : null;
    }

    public static Lexakai get()
    {
        return (Lexakai) Application.get();
    }

    public static void main(String[] arguments)
    {
        new Lexakai().run(arguments);
    }

    /** Map from project folder to project */
    private final HashMap<Folder, LexakaiProject> folderToProject = new HashMap<>();

    /** Java parser for source code */
    private JavaParser parser;

    /** The total number of diagrams created */
    private final MutableCount totalDiagrams = new MutableCount();

    /** All unique types that have been included in a project diagram */
    private final Set<String> types = new HashSet<>();

    public SwitchParser<Boolean> ADD_HTML_ANCHORS =
            booleanSwitchParser(this, "add-html-anchors", "Add HTML anchor tags to markdown indexes")
                    .optional()
                    .defaultValue(true)
                    .build();

    public SwitchParser<Boolean> AUTOMATIC_METHOD_GROUPS =
            booleanSwitchParser(this, "automatic-method-groups", "Automatically group methods")
                    .optional()
                    .defaultValue(true)
                    .build();

    public SwitchParser<Boolean> CREATE_PACKAGE_DIAGRAMS =
            booleanSwitchParser(this, "create-package-diagrams", "Build package diagrams for all public types")
                    .optional()
                    .defaultValue(true)
                    .build();

    public SwitchParser<Boolean> CREATE_SVG_FILES =
            booleanSwitchParser(this, "create-svg-files", "Build .svg files from PlantUML output")
                    .optional()
                    .defaultValue(true)
                    .build();

    public SwitchParser<String> EXCLUDE_PROJECTS =
            stringSwitchParser(this, "exclude-projects", "A comma-separated list of maven coordinates in the form projectId:groupId")
                    .optional()
                    .build();

    public SwitchParser<Boolean> INCLUDE_OBJECT_METHODS =
            booleanSwitchParser(this, "include-object-methods", "Include hashCode(), equals() and toString()")
                    .optional()
                    .defaultValue(false)
                    .build();

    public SwitchParser<Boolean> INCLUDE_PROTECTED_METHODS =
            booleanSwitchParser(this, "include-protected-methods", "Include methods with protected access")
                    .optional()
                    .defaultValue(true)
                    .build();

    public SwitchParser<Integer> JAVADOC_METHOD_COMMENT_MINIMUM_LENGTH =
            integerSwitchParser(this, "javadoc-method-comment-minimum-length", "The minimum comment length for adequate Javadoc coverage of a method")
                    .optional()
                    .defaultValue(64)
                    .build();

    public SwitchParser<Integer> JAVADOC_MINIMUM_METHOD_LINES =
            integerSwitchParser(this, "javadoc-minimum-method-lines", "The minimum number of lines for a method to require a Javadoc comment")
                    .optional()
                    .defaultValue(4)
                    .build();

    public SwitchParser<String> JAVADOC_SECTION_PATTERN =
            stringSwitchParser(this, "javadoc-section-pattern", "regular expression for extracting javadoc section titles")
                    .optional()
                    .defaultValue("<p><b>(.*)</b></p>")
                    .build();

    public SwitchParser<Integer> JAVADOC_SIGNIFICANT_CLASS_MINIMUM_LENGTH =
            integerSwitchParser(this, "javadoc-significant-class-minimum-length", "The minimum length of class that is considered 'significant'")
                    .optional()
                    .defaultValue(2048)
                    .build();

    public SwitchParser<Integer> JAVADOC_TYPE_COMMENT_MINIMUM_LENGTH =
            integerSwitchParser(this, "javadoc-type-comment-minimum-length", "The minimum comment length for adequate Javadoc coverage of a type")
                    .optional()
                    .defaultValue(128)
                    .build();

    public SwitchParser<Integer> JAVADOC_ENUM_COMMENT_MINIMUM_LENGTH =
            integerSwitchParser(this, "javadoc-enum-comment-minimum-length", "The minimum comment length for adequate Javadoc coverage of an enum")
                    .optional()
                    .defaultValue(64)
                    .build();

    public SwitchParser<Folder> OUTPUT_FOLDER =
            folderSwitchParser(this, "output-folder", "Root folder of output")
                    .optional()
                    .defaultValue(Folder.parseFolder(this, "./documentation/lexakai/output"))
                    .build();

    public SwitchParser<Boolean> OVERWRITE_RESOURCES =
            booleanSwitchParser(this, "overwrite-resources", "True to update all resources except settings")
                    .optional()
                    .defaultValue(false)
                    .build();

    public SwitchParser<Boolean> PRINT_DIAGRAMS_TO_CONSOLE =
            booleanSwitchParser(this, "console-output", "Print diagrams to the console")
                    .optional()
                    .defaultValue(false)
                    .build();

    public ArgumentParser<Folder> ROOT_FOLDER =
            folderArgumentParser(this, "Root folder to start at when locating projects")
                    .oneOrMore()
                    .build();

    public SwitchParser<Boolean> SAVE_DIAGRAMS =
            booleanSwitchParser(this, "save", "Save PlantUML diagrams")
                    .optional()
                    .defaultValue(true)
                    .build();

    public SwitchParser<Boolean> SHOW_DIAGRAMS =
            booleanSwitchParser(this, "show-diagrams", "Show created diagrams")
                    .optional()
                    .defaultValue(false)
                    .build();

    public SwitchParser<Boolean> SHOW_DIAGRAM_WARNINGS =
            booleanSwitchParser(this, "show-diagram-warnings", "Show warnings about diagrams as they are processed")
                    .optional()
                    .defaultValue(true)
                    .build();

    public SwitchParser<Boolean> SHOW_JAVADOC_COVERAGE =
            booleanSwitchParser(this, "show-javadoc-coverage", "Show Javadoc coverage for each project as they are processed")
                    .optional()
                    .defaultValue(true)
                    .build();

    public SwitchParser<Boolean> SHOW_JAVADOC_COVERAGE_WARNINGS =
            booleanSwitchParser(this, "show-javadoc-coverage-warnings", "Show Javadoc coverage warnings to help correct issues")
                    .optional()
                    .defaultValue(true)
                    .build();

    public SwitchParser<Boolean> SHOW_JAVADOC_UNCOVERED_TYPES =
            booleanSwitchParser(this, "show-javadoc-uncovered types", "Show list of uncovered types in the summary")
                    .optional()
                    .defaultValue(false)
                    .build();

    public SwitchParser<Traversal> TRAVERSAL =
            enumSwitchParser(this, "traversal", "Traversal of projects", Traversal.class)
                    .optional()
                    .defaultValue(Traversal.RECURSE)
                    .build();

    public SwitchParser<Boolean> UPDATE_README =
            booleanSwitchParser(this, "update-readme", "True to create and update a README.md file")
                    .optional()
                    .defaultValue(false)
                    .build();

    private final StringList exclusions = new StringList();

    @Override
    public String description()
    {
        var variables = kivakit().properties().add("lexakai-version", version().toString());
        var template = PackageResource.packageResource(this, getClass(), "Help.txt").reader().asString();
        return variables.expand(template);
    }

    public LexakaiProject project(Folder folder)
    {
        return folderToProject.get(folder);
    }

    public CopyMode resourceCopyMode()
    {
        return get(OVERWRITE_RESOURCES) ? UPDATE : DO_NOT_OVERWRITE;
    }

    @Override
    protected List<ArgumentParser<?>> argumentParsers()
    {
        return List.of(ROOT_FOLDER);
    }

    @Override
    protected void onRun()
    {
        // get the root folders to locate projects from,
        var roots = commandLine().arguments(ROOT_FOLDER);

        // add any project exclusions
        if (has(EXCLUDE_PROJECTS))
        {
            Collections.addAll(exclusions, get(EXCLUDE_PROJECTS).split(","));
        }

        // create a new Java parser for the root folders,
        parser = newParser(roots);

        // and for each root folder,
        for (var root : roots)
        {
            // build documentation.
            buildDocumentation(root);
        }
    }

    @Override
    protected ObjectSet<SwitchParser<?>> switchParsers()
    {
        return ObjectSet.objectSet(
                ADD_HTML_ANCHORS,
                AUTOMATIC_METHOD_GROUPS,
                CREATE_PACKAGE_DIAGRAMS,
                CREATE_SVG_FILES,
                EXCLUDE_PROJECTS,
                INCLUDE_OBJECT_METHODS,
                INCLUDE_PROTECTED_METHODS,
                JAVADOC_ENUM_COMMENT_MINIMUM_LENGTH,
                JAVADOC_METHOD_COMMENT_MINIMUM_LENGTH,
                JAVADOC_MINIMUM_METHOD_LINES,
                JAVADOC_SECTION_PATTERN,
                JAVADOC_SIGNIFICANT_CLASS_MINIMUM_LENGTH,
                JAVADOC_TYPE_COMMENT_MINIMUM_LENGTH,
                OUTPUT_FOLDER,
                OVERWRITE_RESOURCES,
                PRINT_DIAGRAMS_TO_CONSOLE,
                SAVE_DIAGRAMS,
                SHOW_DIAGRAMS,
                SHOW_DIAGRAM_WARNINGS,
                SHOW_JAVADOC_COVERAGE,
                SHOW_JAVADOC_COVERAGE_WARNINGS,
                SHOW_JAVADOC_UNCOVERED_TYPES,
                TRAVERSAL,
                UPDATE_README);
    }

    private Set<File> buildDependencyDiagrams(Folder root)
    {
        // For each maven dependency tree under the root,
        var files = new HashSet<File>();
        for (var tree : listenTo(new MavenDependencyTreeBuilder(root)).trees())
        {
            // build and save a dependency diagram.
            files.add(new DependencyDiagram(root, outputRoot(root.absolute()), tree).save());
        }
        return files;
    }

    private void buildDocumentation(Folder root)
    {
        // Get the absolute root folder and project,
        var absoluteRoot = root.absolute();

        // build a set of dependency diagrams,
        var outputFiles = new ObjectList<File>();
        outputFiles.addAll(buildDependencyDiagrams(absoluteRoot));

        // create projects for folders under the root,
        projectFolders(absoluteRoot, at ->
        {
            var project = project(absoluteRoot, at);
            if (project != null && !isExcluded(project))
            {
                folderToProject.put(at, project);
            }
        });

        // then for each project,
        projectFolders(absoluteRoot, at ->
        {
            // build UML diagrams.
            var project = project(at);
            if (project != null && !isExcluded(project))
            {
                outputFiles.addAll(outputUmlDiagrams(project));
            }
        });

        // Show detailed Javadoc coverage
        var rootProject = project(absoluteRoot);
        if (rootProject != null && get(SHOW_JAVADOC_COVERAGE))
        {
            announce("");
            announce(AsciiArt.line("Javadoc Coverage"));
            announce("");
            for (var coverage : rootProject.nestedProjectJavadocCoverage())
            {
                announce("Project $", coverage.project().name());
                announce("    $", coverage.coverage());
                if (get(SHOW_JAVADOC_COVERAGE_WARNINGS))
                {
                    var warnings = coverage.warnings();
                    if (warnings.isNonEmpty())
                    {
                        announce(warnings.indented(6).join("\n"));
                    }
                }
            }
        }

        // and summary.
        var list = new StringList();
        list.add("Diagrams: $", totalDiagrams.get());
        list.add("Types: $", types.size());
        list.add("Types per Diagram: ${double}", (double) types.size() / totalDiagrams.get());
        assert rootProject != null;
        list.add("Javadoc Coverage:\n\n$", rootProject.nestedProjectJavadocCoverage()
                .uniqued()
                .sorted()
                .mapped(JavadocCoverage::details)
                .asStringList()
                .join("\n"));

        announce(list.titledBox("Summary"));

        // If the user wants SVG output, and we have some .puml diagrams,
        if (get(CREATE_SVG_FILES) && !outputFiles.isEmpty())
        {
            // then build those files.
            buildSvgFiles(outputFiles);
        }
    }

    private void buildSvgFiles(ObjectList<File> outputFiles)
    {
        // largely I/O bound, so use a larger multiple
        var threads = Integer.toString(Runtime.getRuntime().availableProcessors() * 3);
        var arguments = new StringList();
        arguments.add("-Xmx4G");
        arguments.add("-nbthread");
        arguments.add(threads);
        arguments.add("-progress");
        arguments.add("-tsvg");
        arguments.addAll(outputFiles.asStringList());

        announce("Building SVG files with PlantUML (https://plantuml.com)...");
        var process = listenTo(new JarLauncher()
                .processType(CHILD)
                .arguments(arguments))
                .addJarSource(PackageResource.packageResource(this, getClass(), "plantuml.jar"))
                .redirectTo(CONSOLE)
                .run();

        Processes.waitFor(process);
        System.out.println();
    }

    private boolean isExcluded(LexakaiProject project)
    {
        var coordinates = project.mavenCoordinates();
        return exclusions.contains(coordinates.groupId + ":" + coordinates.artifactId);
    }

    /**
     * @return A parser that can resolve symbols from all projects under all specified roots
     */
    private JavaParser newParser(List<Folder> roots)
    {
        // Create type solver for all source folders under all roots
        var solver = new CombinedTypeSolver();
        roots.forEach(root ->
                projectFolders(root, at ->
                {
                    var project = project(root, at);
                    if (project != null && !isExcluded(project))
                    {
                        if (at.folder("src/main/java").exists())
                        {
                            solver.add(new JavaParserTypeSolver(at.folder("src/main/java").absolute().asJavaFile()));
                        }
                    }
                }));

        // and return a configured parser.
        var configuration = new ParserConfiguration();
        configuration.setSymbolResolver(new JavaSymbolSolver(solver));
        return new JavaParser(configuration);
    }

    private Folder outputRoot(Folder root)
    {
        return get(OUTPUT_FOLDER, root);
    }

    /**
     * Outputs a single UML diagram
     */
    private File outputUmlDiagram(LexakaiClassDiagram diagram)
    {
        // Get the diagram name,
        var diagramName = diagram.identifier();

        // get the UML output folder and read in the lexakai.properties file with diagram titles,
        var diagramFolder = diagram.project().folders().diagramOutput();
        var title = diagram.title();

        // get the uml for the given diagram,
        if (get(SHOW_DIAGRAMS))
        {
            narrate("    Diagram $", diagram.name());
        }
        var uml = diagram.uml(title);
        var builder = IndentingStringBuilder.defaultTextIndenter();
        uml = (builder.lines().isZero() ? "" : builder + "\n\n") + uml;

        // and if the user wants console output,
        if (get(PRINT_DIAGRAMS_TO_CONSOLE))
        {
            // show the diagram on the console.
            System.out.println(AsciiArt.box(diagram.name()));
            System.out.println(uml);
        }

        // and if the user wants to save diagrams,
        if (get(SAVE_DIAGRAMS))
        {
            // create an output file
            var outputFile = diagramFolder.file(diagramName + ".puml");

            // and write the UML to it,
            var output = outputFile.printWriter();
            output.println(uml);
            output.close();
            return outputFile;
        }

        return null;
    }

    /**
     * Parses source code under the project folder and outputs UML diagrams for that
     */
    private ObjectList<File> outputUmlDiagrams(LexakaiProject project)
    {
        // Create a UML project from the source files under the project folder,
        narrate("Project $", project.name());

        // create a documentation folder and install defaults if project is new,
        project.initialize();

        // and copy the project's theme file into the diagram output folder.
        project.files().lexakaiTheme().safeCopyTo(project.folders().diagramOutput(), UPDATE);

        // If the project has source code,
        var outputFiles = new ObjectList<File>();
        if (project.hasSourceCode())
        {
            // then go through each diagram in the project,
            project.diagrams(diagram ->
            {
                // output a UML diagram for it,
                var outputFile = outputUmlDiagram(diagram);
                if (outputFile != null)
                {
                    outputFiles.add(outputFile);
                }

                // add to statistics,
                types.addAll(diagram.qualifiedTypeNames());
                totalDiagrams.increment();
            });
        }

        // and update the README.md index.
        if (get(UPDATE_README))
        {
            project.updateReadMe();
        }

        return outputFiles;
    }

    private LexakaiProject project(Folder root,
                                   Folder projectFolder)
    {
        var project = new LexakaiProject(this, root, projectFolder, outputRoot(root), parser);
        if (project.initialize())
        {
            return listenTo(project)
                    .addHtmlAnchors(get(ADD_HTML_ANCHORS))
                    .includeObjectMethods(get(INCLUDE_OBJECT_METHODS))
                    .includeProtectedMethods(get(INCLUDE_PROTECTED_METHODS))
                    .buildPackageDiagrams(get(CREATE_PACKAGE_DIAGRAMS))
                    .automaticMethodGroups(get(AUTOMATIC_METHOD_GROUPS))
                    .javadocSectionPattern(Pattern.compile(get(JAVADOC_SECTION_PATTERN)));
        }
        else
        {
            return null;
        }
    }

    /**
     * Calls the consumer with each project folder under the root folder and a boolean indicating whether it contains
     * source code
     */
    private void projectFolders(Folder root, Consumer<Folder> consumer)
    {
        // Make sure the root exists,
        ensure(root.exists());

        // then find all the pom or gradle files from the root and return based on those, the set of project folders.
        root.files(file -> file.fileName().name().matches("pom.xml|gradle.properties"), get(TRAVERSAL))
                .stream()
                .map(File::parent)
                .map(Folder::absolute)
                .filter(folder -> !folder.path().join().contains("target"))
                .filter(folder -> !folder.path().join().contains("src/main/resources"))
                .forEach(consumer);
    }
}
