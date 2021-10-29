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
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.filesystem.Folder.Traversal;
import com.telenav.kivakit.kernel.KivaKit;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.collections.set.ObjectSet;
import com.telenav.kivakit.kernel.language.strings.AsciiArt;
import com.telenav.kivakit.kernel.language.strings.formatting.IndentingStringBuilder;
import com.telenav.kivakit.kernel.language.values.count.MutableCount;
import com.telenav.kivakit.kernel.language.values.version.Version;
import com.telenav.kivakit.kernel.language.vm.Processes;
import com.telenav.kivakit.resource.CopyMode;
import com.telenav.kivakit.resource.ResourceProject;
import com.telenav.kivakit.resource.resources.jar.launcher.JarLauncher;
import com.telenav.kivakit.resource.resources.packaged.PackageResource;
import com.telenav.lexakai.dependencies.DependencyDiagram;
import com.telenav.lexakai.dependencies.MavenDependencyTreeBuilder;
import com.telenav.lexakai.javadoc.JavadocCoverage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.telenav.kivakit.commandline.SwitchParser.booleanSwitchParser;
import static com.telenav.kivakit.commandline.SwitchParser.enumSwitchParser;
import static com.telenav.kivakit.commandline.SwitchParser.integerSwitchParser;
import static com.telenav.kivakit.commandline.SwitchParser.stringSwitchParser;
import static com.telenav.kivakit.commandline.SwitchParser.versionSwitchParser;
import static com.telenav.kivakit.filesystem.Folder.folderArgumentParser;
import static com.telenav.kivakit.filesystem.Folder.folderSwitchParser;
import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.kivakit.resource.CopyMode.DO_NOT_OVERWRITE;
import static com.telenav.kivakit.resource.CopyMode.UPDATE;
import static com.telenav.kivakit.resource.resources.jar.launcher.JarLauncher.ProcessType.CHILD;
import static com.telenav.kivakit.resource.resources.jar.launcher.JarLauncher.RedirectTo.CONSOLE;

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
public class Lexakai extends Application
{
    public static final SwitchParser<Boolean> ADD_HTML_ANCHORS =
            booleanSwitchParser("add-html-anchors", "Add HTML anchor tags to markdown indexes")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> AUTOMATIC_METHOD_GROUPS =
            booleanSwitchParser("automatic-method-groups", "Automatically group methods")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> CREATE_PACKAGE_DIAGRAMS =
            booleanSwitchParser("create-package-diagrams", "Build package diagrams for all public types")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> CREATE_SVG_FILES =
            booleanSwitchParser("create-svg-files", "Build .svg files from PlantUML output")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> INCLUDE_OBJECT_METHODS =
            booleanSwitchParser("include-object-methods", "Include hashCode(), equals() and toString()")
                    .optional()
                    .defaultValue(false)
                    .build();

    public static final SwitchParser<Boolean> INCLUDE_PROTECTED_METHODS =
            booleanSwitchParser("include-protected-methods", "Include methods with protected access")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Integer> JAVADOC_METHOD_COMMENT_MINIMUM_LENGTH =
            integerSwitchParser("javadoc-method-comment-minimum-length", "The minimum comment length for adequate Javadoc coverage of a method")
                    .optional()
                    .defaultValue(64)
                    .build();

    public static final SwitchParser<Integer> JAVADOC_MINIMUM_METHOD_LINES =
            integerSwitchParser("javadoc-minimum-method-lines", "The minimum number of lines for a method to require a Javadoc comment")
                    .optional()
                    .defaultValue(4)
                    .build();

    public static final SwitchParser<String> JAVADOC_SECTION_PATTERN =
            stringSwitchParser("javadoc-section-pattern", "regular expression for extracting javadoc section titles")
                    .optional()
                    .defaultValue("<p><b>(.*)</b></p>")
                    .build();

    public static final SwitchParser<Integer> JAVADOC_SIGNIFICANT_CLASS_MINIMUM_LENGTH =
            integerSwitchParser("javadoc-significant-class-minimum-length", "The minimum length of class that is considered 'significant'")
                    .optional()
                    .defaultValue(2048)
                    .build();

    public static final SwitchParser<Integer> JAVADOC_TYPE_COMMENT_MINIMUM_LENGTH =
            integerSwitchParser("javadoc-type-comment-minimum-length", "The minimum comment length for adequate Javadoc coverage of a type")
                    .optional()
                    .defaultValue(128)
                    .build();

    public static final SwitchParser<Integer> JAVADOC_ENUM_COMMENT_MINIMUM_LENGTH =
            integerSwitchParser("javadoc-enum-comment-minimum-length", "The minimum comment length for adequate Javadoc coverage of an enum")
                    .optional()
                    .defaultValue(64)
                    .build();

    public static final SwitchParser<Folder> OUTPUT_FOLDER =
            folderSwitchParser("output-folder", "Root folder of output")
                    .optional()
                    .defaultValue(Folder.parse("./documentation/lexakai/output"))
                    .build();

    public static final SwitchParser<Boolean> OVERWRITE_RESOURCES =
            booleanSwitchParser("overwrite-resources", "True to update all resources except settings")
                    .optional()
                    .defaultValue(false)
                    .build();

    public static final SwitchParser<Boolean> PRINT_DIAGRAMS_TO_CONSOLE =
            booleanSwitchParser("console-output", "Print diagrams to the console")
                    .optional()
                    .defaultValue(false)
                    .build();

    public static final SwitchParser<Version> PROJECT_VERSION =
            versionSwitchParser("project-version", "Version of project used when generating markdown")
                    .optional()
                    .build();

    public static final ArgumentParser<Folder> ROOT_FOLDER =
            folderArgumentParser("Root folder to start at when locating projects")
                    .oneOrMore()
                    .build();

    public static final SwitchParser<Boolean> SAVE_DIAGRAMS =
            booleanSwitchParser("save", "Save PlantUML diagrams")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> SHOW_DIAGRAMS =
            booleanSwitchParser("show-diagrams", "Show created diagrams")
                    .optional()
                    .defaultValue(false)
                    .build();

    public static final SwitchParser<Boolean> SHOW_DIAGRAM_WARNINGS =
            booleanSwitchParser("show-diagram-warnings", "Show warnings about diagrams as they are processed")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> SHOW_JAVADOC_COVERAGE =
            booleanSwitchParser("show-javadoc-coverage", "Show Javadoc coverage for each project as they are processed")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> SHOW_JAVADOC_COVERAGE_WARNINGS =
            booleanSwitchParser("show-javadoc-coverage-warnings", "Show Javadoc coverage warnings to help correct issues")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> SHOW_JAVADOC_UNCOVERED_TYPES =
            booleanSwitchParser("show-javadoc-uncovered types", "Show list of uncovered types in the summary")
                    .optional()
                    .defaultValue(false)
                    .build();

    public static final SwitchParser<Traversal> TRAVERSAL =
            enumSwitchParser("traversal", "Traversal of projects", Traversal.class)
                    .optional()
                    .defaultValue(Traversal.RECURSE)
                    .build();

    public static final SwitchParser<Boolean> UPDATE_README =
            booleanSwitchParser("update-readme", "True to create and update a README.md file")
                    .optional()
                    .defaultValue(false)
                    .build();

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

    protected Lexakai()
    {
        super(ResourceProject.get());
    }

    @Override
    public String description()
    {
        var variables = KivaKit.get().properties().add("lexakai-version", version().toString());
        var template = PackageResource.of(getClass(), "Help.txt").reader().string();
        return variables.expand(template);
    }

    public LexakaiProject project(Folder folder)
    {
        return folderToProject.get(folder);
    }

    public CopyMode resourceCopyMode()
    {
        return get(Lexakai.OVERWRITE_RESOURCES) ? UPDATE : DO_NOT_OVERWRITE;
    }

    @Override
    protected List<ArgumentParser<?>> argumentParsers()
    {
        return List.of(ROOT_FOLDER);
    }

    @Override
    protected void onRun()
    {
        // Show our command line parameters,
        announce(commandLineDescription("Lexakai"));

        // get the root folders to locate projects from,
        var roots = commandLine().arguments(ROOT_FOLDER);

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
        return ObjectSet.of(
                ADD_HTML_ANCHORS,
                AUTOMATIC_METHOD_GROUPS,
                CREATE_PACKAGE_DIAGRAMS,
                CREATE_SVG_FILES,
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
                PROJECT_VERSION,
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
            if (project != null)
            {
                folderToProject.put(at, project);
            }
        });

        // then for each project,
        projectFolders(absoluteRoot, at ->
        {
            // build UML diagrams.
            var project = project(at);
            if (project != null)
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
        list.add("Javadoc Coverage:\n\n$", rootProject.nestedProjectJavadocCoverage()
                .uniqued()
                .sorted()
                .mapped(JavadocCoverage::details)
                .asStringList()
                .join("\n"));

        announce(list.titledBox("Summary"));

        // If the user wants SVG output and we have some .puml diagrams,
        if (get(CREATE_SVG_FILES) && !outputFiles.isEmpty())
        {
            // then build those files.
            buildSvgFiles(outputFiles);
        }
    }

    private void buildSvgFiles(ObjectList<File> outputFiles)
    {
        var arguments = new StringList();
        arguments.add("-nbthread");
        arguments.add("12");
        arguments.add("-progress");
        arguments.add("-tsvg");
        arguments.addAll(outputFiles.asStringList());

        announce("Building SVG files with PlantUML (https://plantuml.com)...");
        var process = listenTo(new JarLauncher()
                .processType(CHILD)
                .arguments(arguments))
                .addJarSource(PackageResource.of(getClass(), "plantuml.jar"))
                .redirectTo(CONSOLE)
                .run();

        Processes.waitFor(process);
        System.out.println();
    }

    /**
     * @return A parser that can resolve symbols from all projects under all specified roots
     */
    private JavaParser newParser(List<Folder> roots)
    {
        // Create type solver for all source folders under all roots
        var solver = new CombinedTypeSolver();
        roots.forEach(root ->
                projectFolders(root, projectFolder ->
                {
                    if (projectFolder.folder("src/main/java").exists())
                    {
                        solver.add(new JavaParserTypeSolver(projectFolder.folder("src/main/java").absolute().asJavaFile()));
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

        // and if the user wants output to the console,
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
        var project = new LexakaiProject(this, get(PROJECT_VERSION), root, projectFolder, outputRoot(root), parser);
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
                .filter(Objects::nonNull)
                .forEach(consumer);
    }
}
