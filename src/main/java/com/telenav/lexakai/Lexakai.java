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
import com.telenav.lexakai.LexakaiProject.JavadocCoverage;
import com.telenav.lexakai.dependencies.DependencyDiagram;
import com.telenav.lexakai.dependencies.MavenDependencyTreeBuilder;

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
    public static void main(final String[] arguments)
    {
        new Lexakai().run(arguments);
    }

    final SwitchParser<Integer> JAVADOC_TYPE_COMMENT_MINIMUM_LENGTH =
            SwitchParser.integerSwitch("javadoc-type-comment-minimum-length", "THe minimum text length for Javadoc coverage of a type")
                    .optional()
                    .defaultValue(128)
                    .build();

    private final ArgumentParser<Folder> ROOT_FOLDER =
            Folder.folderArgument("Root folder to start at when locating projects")
                    .oneOrMore()
                    .build();

    private final SwitchParser<Boolean> ADD_HTML_ANCHORS =
            SwitchParser.booleanSwitch("add-html-anchors", "Add HTML anchor tags to indexed markdown titles")
                    .optional()
                    .defaultValue(true)
                    .build();

    private final SwitchParser<Boolean> CREATE_PACKAGE_DIAGRAMS =
            SwitchParser.booleanSwitch("create-package-diagrams", "Build whole-package diagrams for all public types")
                    .optional()
                    .defaultValue(true)
                    .build();

    private final SwitchParser<Traversal> TRAVERSAL =
            SwitchParser.enumSwitch("traversal", "Traversal of projects", Traversal.class)
                    .optional()
                    .defaultValue(Traversal.RECURSE)
                    .build();

    private final SwitchParser<Boolean> INCLUDE_OBJECT_METHODS =
            SwitchParser.booleanSwitch("include-object-methods", "Include hashCode(), equals() and toString()")
                    .optional()
                    .defaultValue(false)
                    .build();

    private final SwitchParser<Version> PROJECT_VERSION =
            SwitchParser.versionSwitch("project-version", "Version of project used in generating links in README.md indexes")
                    .optional()
                    .defaultValue(KivaKit.get().version())
                    .build();

    private final SwitchParser<Boolean> INCLUDE_PROTECTED_METHODS =
            SwitchParser.booleanSwitch("include-protected-methods", "Include methods with protected access")
                    .optional()
                    .defaultValue(true)
                    .build();

    private final SwitchParser<Boolean> SAVE_DIAGRAMS =
            SwitchParser.booleanSwitch("save", "True to save diagrams, false to write them to the console")
                    .optional()
                    .defaultValue(true)
                    .build();

    private final SwitchParser<Boolean> AUTOMATIC_METHOD_GROUPS =
            SwitchParser.booleanSwitch("automatic-method-groups", "True to automatically group methods")
                    .optional()
                    .defaultValue(true)
                    .build();

    private final SwitchParser<Boolean> UPDATE_README =
            SwitchParser.booleanSwitch("update-readme", "True to create and update a README.md file")
                    .optional()
                    .defaultValue(false)
                    .build();

    private final SwitchParser<Boolean> JAVADOC_COVERAGE =
            SwitchParser.booleanSwitch("javadoc-coverage", "True to show Javadoc coverage for the types in each project")
                    .optional()
                    .defaultValue(true)
                    .build();

    private final SwitchParser<String> JAVADOC_SECTION_PATTERN =
            SwitchParser.stringSwitch("javadoc-section-pattern", "regular expression for extracting javadoc section titles")
                    .optional()
                    .defaultValue("<p><b>(.*)</b></p>")
                    .build();

    private final SwitchParser<Boolean> CREATE_SVG_FILES =
            SwitchParser.booleanSwitch("create-svg-files", "True to build .svg files from PlantUML output")
                    .optional()
                    .defaultValue(true)
                    .build();

    private final SwitchParser<Boolean> PRINT_DIAGRAMS_TO_CONSOLE =
            SwitchParser.booleanSwitch("console", "True to write to the console")
                    .optional()
                    .defaultValue(false)
                    .build();

    /** The total number of diagrams created */
    private final MutableCount totalDiagrams = new MutableCount();

    /** All unique types that have been included in a project diagram */
    private final Set<String> types = new HashSet<>();

    /** Java parser for source code */
    private JavaParser parser;

    /** Map from project folder to project */
    private final HashMap<Folder, LexakaiProject> folderToProject = new HashMap<>();

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
        announce(commandLineDescription("Lexakai"));

        // Get the root folder to locate projects from,
        final var roots = commandLine().arguments(ROOT_FOLDER);
        parser = newParser(roots);

        // and for each root folder,
        final var outputFiles = new ObjectList<File>();
        final var coverage = new ObjectList<JavadocCoverage>();
        for (final var root : roots)
        {
            // convert it to an absolute path,
            final var absoluteRoot = root.absolute();

            // build a set of dependency diagrams,
            outputFiles.addAll(buildDependencyDiagrams(absoluteRoot));

            // create projects for folders under the root,
            projectFolders(absoluteRoot, projectFolder ->
            {
                final var project = project(absoluteRoot, projectFolder);
                folderToProject.put(projectFolder, project);
            });

            // the for each project,
            projectFolders(absoluteRoot, projectFolder ->
            {
                // build UML diagrams.
                final var project = project(projectFolder);
                outputFiles.addAll(outputUmlDiagrams(project));
                coverage.addAll(project.javadocCoverage());
            });
        }

        // If the user wants SVG output and we have some .puml diagrams,
        if (get(CREATE_SVG_FILES) && !outputFiles.isEmpty())
        {
            // then build those files.
            buildSvgFiles(outputFiles);
        }

        final var list = new StringList();
        list.add("Diagrams: $", totalDiagrams.get());
        list.add("Types: $", types.size());
        list.add("Types per Diagram: ${double}", (double) types.size() / totalDiagrams.get());
        list.add("Javadoc Coverage:\n\n$", coverage
                .uniqued()
                .sorted()
                .mapped(JavadocCoverage::detailed)
                .asStringList()
                .join("\n"));

        announce(list.titledBox("Summary"));
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
                JAVADOC_COVERAGE,
                JAVADOC_TYPE_COMMENT_MINIMUM_LENGTH,
                JAVADOC_SECTION_PATTERN,
                PRINT_DIAGRAMS_TO_CONSOLE,
                PROJECT_VERSION,
                TRAVERSAL,
                SAVE_DIAGRAMS,
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
        narrate("    Diagram $", diagram.name());
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
    private ObjectList<File> outputUmlDiagrams(
            final LexakaiProject project)
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

        // show Javadoc coverage
        if (get(JAVADOC_COVERAGE))
        {
            for (final var coverage : project.javadocCoverage())
            {
                information(coverage.description().indented(4).join("\n"));
            }
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
