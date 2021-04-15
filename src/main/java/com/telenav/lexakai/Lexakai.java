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
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.telenav.kivakit.core.application.Application;
import com.telenav.kivakit.core.commandline.ArgumentParser;
import com.telenav.kivakit.core.commandline.SwitchParser;
import com.telenav.kivakit.core.configuration.lookup.Lookup;
import com.telenav.kivakit.core.filesystem.File;
import com.telenav.kivakit.core.filesystem.Folder;
import com.telenav.kivakit.core.filesystem.Folder.Traversal;
import com.telenav.kivakit.core.kernel.KivaKit;
import com.telenav.kivakit.core.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.strings.AsciiArt;
import com.telenav.kivakit.core.kernel.language.strings.formatting.IndentingStringBuilder;
import com.telenav.kivakit.core.kernel.language.values.count.MutableCount;
import com.telenav.kivakit.core.kernel.language.values.version.Version;
import com.telenav.kivakit.core.kernel.language.vm.Processes;
import com.telenav.kivakit.core.resource.project.CoreResourceProject;
import com.telenav.kivakit.core.resource.resources.jar.launcher.JarLauncher;
import com.telenav.kivakit.core.resource.resources.packaged.PackageResource;
import com.telenav.lexakai.dependencies.DependencyDiagram;
import com.telenav.lexakai.dependencies.MavenDependencyTreeBuilder;
import com.telenav.lexakai.javadoc.JavadocCoverage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.resource.resources.jar.launcher.JarLauncher.ProcessType.CHILD;
import static com.telenav.kivakit.core.resource.resources.jar.launcher.JarLauncher.RedirectTo.CONSOLE;

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
            SwitchParser.booleanSwitch("add-html-anchors", "Add HTML anchor tags to indexed markdown titles")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> AUTOMATIC_METHOD_GROUPS =
            SwitchParser.booleanSwitch("automatic-method-groups", "True to automatically group methods")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> CREATE_PACKAGE_DIAGRAMS =
            SwitchParser.booleanSwitch("create-package-diagrams", "Build whole-package diagrams for all public types")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> CREATE_SVG_FILES =
            SwitchParser.booleanSwitch("create-svg-files", "True to build .svg files from PlantUML output")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> INCLUDE_OBJECT_METHODS =
            SwitchParser.booleanSwitch("include-object-methods", "Include hashCode(), equals() and toString()")
                    .optional()
                    .defaultValue(false)
                    .build();

    public static final SwitchParser<Boolean> INCLUDE_PROTECTED_METHODS =
            SwitchParser.booleanSwitch("include-protected-methods", "Include methods with protected access")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Integer> JAVADOC_METHOD_COMMENT_MINIMUM_LENGTH =
            SwitchParser.integerSwitch("javadoc-method-comment-minimum-length", "The minimum comment length for adequate Javadoc coverage of a method")
                    .optional()
                    .defaultValue(64)
                    .build();

    public static final SwitchParser<Integer> JAVADOC_MINIMUM_METHOD_LINES =
            SwitchParser.integerSwitch("javadoc-minimum-method-lines", "The minimum number of lines for a method to require a Javadoc comment")
                    .optional()
                    .defaultValue(4)
                    .build();

    public static final SwitchParser<String> JAVADOC_SECTION_PATTERN =
            SwitchParser.stringSwitch("javadoc-section-pattern", "regular expression for extracting javadoc section titles")
                    .optional()
                    .defaultValue("<p><b>(.*)</b></p>")
                    .build();

    public static final SwitchParser<Integer> JAVADOC_SIGNIFICANT_CLASS_MINIMUM_LENGTH =
            SwitchParser.integerSwitch("javadoc-significant-class-minimum-length", "The minimum length of class that is considered 'significant'")
                    .optional()
                    .defaultValue(2048)
                    .build();

    public static final SwitchParser<Integer> JAVADOC_TYPE_COMMENT_MINIMUM_LENGTH =
            SwitchParser.integerSwitch("javadoc-type-comment-minimum-length", "The minimum comment length for adequate Javadoc coverage of a type")
                    .optional()
                    .defaultValue(128)
                    .build();

    public static final SwitchParser<Integer> JAVADOC_ENUM_COMMENT_MINIMUM_LENGTH =
            SwitchParser.integerSwitch("javadoc-enum-comment-minimum-length", "The minimum comment length for adequate Javadoc coverage of an enum")
                    .optional()
                    .defaultValue(64)
                    .build();

    public static final SwitchParser<Boolean> PRINT_DIAGRAMS_TO_CONSOLE =
            SwitchParser.booleanSwitch("console", "True to write to the console")
                    .optional()
                    .defaultValue(false)
                    .build();

    public static final SwitchParser<Version> PROJECT_VERSION =
            SwitchParser.versionSwitch("project-version", "Version of project used in generating links in README.md indexes")
                    .optional()
                    .defaultValue(KivaKit.get().version())
                    .build();

    public static final ArgumentParser<Folder> ROOT_FOLDER =
            Folder.folderArgument("Root folder to start at when locating projects")
                    .oneOrMore()
                    .build();

    public static final SwitchParser<Boolean> SAVE_DIAGRAMS =
            SwitchParser.booleanSwitch("save", "True to save diagrams, false to write them to the console")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> SHOW_DIAGRAMS =
            SwitchParser.booleanSwitch("show-diagrams", "Show what diagrams are created")
                    .optional()
                    .defaultValue(false)
                    .build();

    public static final SwitchParser<Boolean> SHOW_DIAGRAM_WARNINGS =
            SwitchParser.booleanSwitch("show-diagram-warnings", "Show warnings about diagrams as they are processed")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> SHOW_JAVADOC_COVERAGE =
            SwitchParser.booleanSwitch("show-javadoc-coverage", "Show Javadoc coverage for each project as they are processed")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> SHOW_JAVADOC_COVERAGE_WARNINGS =
            SwitchParser.booleanSwitch("show-javadoc-coverage-warnings", "Show Javadoc coverage warnings to help correct issues")
                    .optional()
                    .defaultValue(true)
                    .build();

    public static final SwitchParser<Boolean> SHOW_JAVADOC_UNCOVERED_TYPES =
            SwitchParser.booleanSwitch("show-javadoc-uncovered types", "Show list of uncovered types in the summary")
                    .optional()
                    .defaultValue(false)
                    .build();

    public static final SwitchParser<Traversal> TRAVERSAL =
            SwitchParser.enumSwitch("traversal", "Traversal of projects", Traversal.class)
                    .optional()
                    .defaultValue(Traversal.RECURSE)
                    .build();

    public static final SwitchParser<Boolean> UPDATE_README =
            SwitchParser.booleanSwitch("update-readme", "True to create and update a README.md file")
                    .optional()
                    .defaultValue(false)
                    .build();

    public static Lexakai get()
    {
        return Lookup.global().locate(Lexakai.class);
    }

    public static void main(final String[] arguments)
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
        super(CoreResourceProject.get());
    }

    @Override
    public String description()
    {
        final var variables = KivaKit.get().properties().add("lexakai-version", version().toString());
        final var template = PackageResource.of(getClass(), "Help.txt").reader().string();
        return variables.expand(template);
    }

    public LexakaiProject project(final Folder folder)
    {
        return folderToProject.get(folder);
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
        final var roots = commandLine().arguments(ROOT_FOLDER);

        // create a new Java parser for the root folders,
        parser = newParser(roots);

        // and for each root folder,
        for (final var root : roots)
        {
            // build documentation.
            buildDocumentation(root);
        }
    }

    @Override
    protected Set<SwitchParser<?>> switchParsers()
    {
        return Set.of(
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

    private Set<File> buildDependencyDiagrams(final Folder root)
    {
        // For each maven dependency tree under the root,
        final var files = new HashSet<File>();
        for (final var tree : listenTo(new MavenDependencyTreeBuilder(root)).trees())
        {
            // build and save a dependency diagram.
            files.add(new DependencyDiagram(tree).save());
        }
        return files;
    }

    private void buildDocumentation(final Folder root)
    {
        // Get the absolute root folder and project,
        final var absoluteRoot = root.absolute();

        // build a set of dependency diagrams,
        final var outputFiles = new ObjectList<File>();
        outputFiles.addAll(buildDependencyDiagrams(absoluteRoot));

        // create projects for folders under the root,
        projectFolders(absoluteRoot, at -> folderToProject.put(at, project(absoluteRoot, at)));

        // then for each project,
        projectFolders(absoluteRoot, at ->
        {
            // build UML diagrams.
            outputFiles.addAll(outputUmlDiagrams(project(at)));
        });

        // Show detailed Javadoc coverage
        final var rootProject = project(absoluteRoot);
        if (get(SHOW_JAVADOC_COVERAGE))
        {
            announce("");
            announce(AsciiArt.line("Javadoc Coverage"));
            announce("");
            for (final var coverage : rootProject.nestedProjectJavadocCoverage())
            {
                announce("Project $", coverage.project().name());
                announce("    $", coverage.coverage());
                if (get(SHOW_JAVADOC_COVERAGE_WARNINGS))
                {
                    final var warnings = coverage.warnings();
                    if (warnings.isNonEmpty())
                    {
                        announce(warnings.indented(6).join("\n"));
                    }
                }
            }
        }

        // and summary.
        final var list = new StringList();
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

    private void buildSvgFiles(final ObjectList<File> outputFiles)
    {
        final var arguments = new StringList();
        arguments.add("-nbthread");
        arguments.add("12");
        arguments.add("-progress");
        arguments.add("-tsvg");
        arguments.addAll(outputFiles.asStringList());

        announce("Building SVG files with PlantUML (http://plantuml.com)...");
        final var process = listenTo(new JarLauncher()
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
    private JavaParser newParser(final List<Folder> roots)
    {
        // Create type solver for all source folders under all roots
        final var solver = new CombinedTypeSolver();
        roots.forEach(root ->
                projectFolders(root, projectFolder ->
                {
                    if (projectFolder.folder("src").exists())
                    {
                        solver.add(new JavaParserTypeSolver(projectFolder.folder("src/main/java").absolute().asJavaFile()));
                    }
                }));

        // and return a configured parser.
        final var configuration = new ParserConfiguration();
        configuration.setSymbolResolver(new JavaSymbolSolver(solver));
        return new JavaParser(configuration);
    }

    /**
     * Outputs a single UML diagram
     */
    private File outputUmlDiagram(final LexakaiClassDiagram diagram)
    {
        // Get the diagram name,
        final var diagramName = diagram.identifier();

        // get the UML output folder and read in the lexakai.properties file with diagram titles,
        final var diagramFolder = diagram.project().diagramFolder().mkdirs();
        final var title = diagram.title();

        // get the uml for the given diagram,
        if (get(SHOW_DIAGRAMS))
        {
            narrate("    Diagram $", diagram.name());
        }
        var uml = diagram.uml(title);
        final var builder = IndentingStringBuilder.defaultTextIndenter();
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
            final var outputFile = diagramFolder.file(diagramName + ".puml");

            // and write the UML to it,
            final var output = outputFile.printWriter();
            output.println(uml);
            output.close();
            return outputFile;
        }

        return null;
    }

    /**
     * Parses source code under the project folder and outputs UML diagrams for that
     */
    private ObjectList<File> outputUmlDiagrams(final LexakaiProject project)
    {
        // Create a UML project from the source files under the project folder,
        narrate("Project $", project.name());

        // create a documentation folder and install defaults if project is new,
        project.initialize();

        // If the project has source code,
        final var outputFiles = new ObjectList<File>();
        if (project.hasSourceCode())
        {
            // then go through each diagram in the project,
            project.diagrams(diagram ->
            {
                // output a UML diagram for it,
                final var outputFile = outputUmlDiagram(diagram);
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

    private LexakaiProject project(final Folder root,
                                   final Folder projectFolder)
    {
        return listenTo(new LexakaiProject(this, get(PROJECT_VERSION), root, projectFolder, parser))
                .addHtmlAnchors(get(ADD_HTML_ANCHORS))
                .includeObjectMethods(get(INCLUDE_OBJECT_METHODS))
                .includeProtectedMethods(get(INCLUDE_PROTECTED_METHODS))
                .buildPackageDiagrams(get(CREATE_PACKAGE_DIAGRAMS))
                .automaticMethodGroups(get(AUTOMATIC_METHOD_GROUPS))
                .javadocSectionPattern(Pattern.compile(get(JAVADOC_SECTION_PATTERN)));
    }

    /**
     * Calls the consumer with each project folder under the root folder and a boolean indicating whether it contains
     * source code
     */
    private void projectFolders(final Folder root, final Consumer<Folder> consumer)
    {
        // Make sure the root exists,
        ensure(root.exists());

        // then find all the pom or gradle files from the root and return based on those, the set of project folders.
        root.files(file -> file.fileName().name().matches("pom.xml|gradle.properties"), get(TRAVERSAL))
                .stream()
                .map(File::parent)
                .map(Folder::absolute)
                .forEach(consumer);
    }
}
