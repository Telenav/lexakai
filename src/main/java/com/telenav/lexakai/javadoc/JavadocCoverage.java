package com.telenav.lexakai.javadoc;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.collections.map.string.VariableMap;
import com.telenav.kivakit.core.kernel.language.strings.Align;
import com.telenav.kivakit.core.kernel.language.strings.Strip;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import com.telenav.kivakit.core.kernel.messaging.Message;
import com.telenav.lexakai.Lexakai;
import com.telenav.lexakai.LexakaiProject;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.library.Annotations;
import org.jetbrains.annotations.NotNull;

import static com.telenav.lexakai.Lexakai.JAVADOC_ENUM_COMMENT_MINIMUM_LENGTH;
import static com.telenav.lexakai.Lexakai.JAVADOC_METHOD_COMMENT_MINIMUM_LENGTH;
import static com.telenav.lexakai.Lexakai.JAVADOC_MINIMUM_METHOD_LINES;
import static com.telenav.lexakai.Lexakai.JAVADOC_SIGNIFICANT_CLASS_MINIMUM_LENGTH;
import static com.telenav.lexakai.Lexakai.JAVADOC_TYPE_COMMENT_MINIMUM_LENGTH;
import static com.telenav.lexakai.Lexakai.SHOW_JAVADOC_UNCOVERED_TYPES;

/**
 * @author jonathanl (shibo)
 */
public class JavadocCoverage implements Comparable<JavadocCoverage>
{
    private final LexakaiProject project;

    private final StringList significantUndocumentedClasses = new StringList();

    private final StringList undocumentedClasses = new StringList();

    private final StringList warnings = new StringList();

    private int totalTypes;

    private int totalCoveredTypes;

    private int totalMethods;

    private int totalCoveredMethods;

    private Percent typeCoverage;

    private Percent methodCoverage;

    private Percent projectCoverage;

    public JavadocCoverage(final LexakaiProject project)
    {
        this.project = project;
    }

    public void add(final TypeDeclaration<?> type)
    {
        final var fullName = type.getFullyQualifiedName();
        if (fullName.isPresent() && (type.isPublic() || type.isProtected()) && !fullName.get().endsWith("Test"))
        {
            totalTypes++;

            var requiredLength = Lexakai.get().get(JAVADOC_TYPE_COMMENT_MINIMUM_LENGTH);

            final var javadoc = type.getJavadoc();
            final var isSignificant = type.toString().length() > Lexakai.get().get(JAVADOC_SIGNIFICANT_CLASS_MINIMUM_LENGTH);
            final var significance = isSignificant ? "=>  " : "    ";
            final var typeName = Strip.packagePrefix(fullName.get());
            var isCovered = true;
            var typeWarning = false;
            if (javadoc.isPresent())
            {
                if (isJavadocComplete(type))
                {
                    isCovered = true;
                }
                else
                {
                    if (type.isEnumDeclaration())
                    {
                        requiredLength = Lexakai.get().get(JAVADOC_ENUM_COMMENT_MINIMUM_LENGTH);
                    }
                    final var text = javadoc.get().toText();
                    if (text.length() < requiredLength)
                    {
                        isCovered = false;
                        typeWarning = true;
                        warnings.add("${string}$: Javadoc coverage is only $ characters (minimum is $)",
                                significance, typeName, text.length(), requiredLength);
                    }
                }

                final var methodWarnings = new StringList();
                for (final var method : type.getMethods())
                {
                    if (method.isPublic() || method.isProtected())
                    {
                        totalMethods++;
                        if (methodCovered(method, methodWarnings))
                        {
                            totalCoveredMethods++;
                        }
                    }
                }
                if (!methodWarnings.isEmpty())
                {
                    if (!typeWarning)
                    {
                        warnings.add(typeName);
                    }
                    warnings.addAll(methodWarnings);
                }
            }
            else
            {
                isCovered = false;
                warnings.add("${string}$: Javadoc is missing", significance, typeName);
            }

            if (isCovered)
            {
                totalCoveredTypes++;
            }
            else
            {
                undocumentedClasses.add(typeName);
                if (isSignificant)
                {
                    significantUndocumentedClasses.add(typeName);
                }
            }
        }
    }

    public void addToVariableMap(final VariableMap<String> variables)
    {
        variables.put("project-javadoc-coverage", projectCoverage()
                + ".  \n  \n&nbsp; &nbsp; " + meterMarkdown());
        final var undocumented = significantUndocumentedClasses;
        variables.put("project-undocumented-classes",
                undocumented.isEmpty() ? "" : "The following significant classes are undocumented:  \n\n" +
                        undocumented.prefixedWith("- ").join("  \n"));
    }

    @Override
    public int compareTo(@NotNull final JavadocCoverage that)
    {
        return projectCoverage().compareTo(that.projectCoverage());
    }

    public String coverage()
    {
        return Message.format("$ ($ types, $ methods)", projectCoverage(), typeCoverage(), methodCoverage());
    }

    public String details()
    {
        return summary().join("\n");
    }

    public LexakaiProject project()
    {
        return project;
    }

    public Percent projectCoverage()
    {
        compute();
        return projectCoverage;
    }

    public String projectCoverageMeter()
    {
        return "&nbsp; " + meterMarkdown() + " &nbsp; &nbsp; " + project.link();
    }

    public StringList summary()
    {
        final var list = new StringList();
        list.add(summaryCoverage());
        if (Lexakai.get().get(SHOW_JAVADOC_UNCOVERED_TYPES))
        {
            if (!undocumentedClasses.isEmpty())
            {
                list.add(undocumentedClasses.prefixedWith("    Uncovered: ").join("\n"));
            }
        }
        return list;
    }

    @Override
    public String toString()
    {
        return coverage();
    }

    public StringList warnings()
    {
        return warnings;
    }

    private void compute()
    {
        if (typeCoverage == null)
        {
            typeCoverage = totalTypes == 0 ? Percent._0 : Percent.of(100.0 * totalCoveredTypes / totalTypes);
            methodCoverage = totalMethods == 0 ? Percent._100 : Percent.of(100.0 * totalCoveredMethods / totalMethods);
            projectCoverage = typeCoverage.plus(methodCoverage).dividedBy(2.0);
        }
    }

    private boolean isJavadocComplete(final NodeWithAnnotations<?> node)
    {
        final var optionalAnnotation = node.getAnnotationByClass(LexakaiJavadoc.class);
        if (optionalAnnotation.isPresent())
        {
            return Annotations.booleanValue(optionalAnnotation.get(), "complete", false);
        }
        return false;
    }

    private String meterMarkdown()
    {
        return LexakaiProject.meterMarkdownForPercent(projectCoverage());
    }

    private Percent methodCoverage()
    {
        compute();
        return methodCoverage;
    }

    private boolean methodCovered(final MethodDeclaration method, final StringList warnings)
    {
        if (!isJavadocComplete(method))
        {
            final var lines = method.getBody().toString().split("\n").length;
            if (lines > Lexakai.get().get(JAVADOC_MINIMUM_METHOD_LINES))
            {
                final var javadoc = method.getJavadoc();
                if (javadoc.isEmpty())
                {
                    warnings.add("    $(): Javadoc missing", method.getName());
                    return false;
                }
                else
                {
                    final var length = javadoc.get().toText().length();
                    final var minimumLength = Lexakai.get().get(JAVADOC_METHOD_COMMENT_MINIMUM_LENGTH);
                    if (length < minimumLength)
                    {
                        warnings.add("    $(): Javadoc coverage of $ characters is insufficient (minimum is $)",
                                method.getName(), length, minimumLength);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private String summaryCoverage()
    {
        compute();
        return Message.format("$: $", Align.right(project.name(), 32, ' '), coverage());
    }

    private Percent typeCoverage()
    {
        compute();
        return typeCoverage;
    }
}
