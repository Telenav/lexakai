package com.telenav.lexakai.javadoc;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.collections.map.VariableMap;
import com.telenav.kivakit.core.string.Align;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.string.Strip;
import com.telenav.kivakit.core.value.level.Percent;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.lexakai.Lexakai;
import com.telenav.lexakai.LexakaiProject;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.library.Annotations;
import org.jetbrains.annotations.NotNull;

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

    public JavadocCoverage(LexakaiProject project)
    {
        this.project = project;
    }

    public void add(TypeDeclaration<?> type)
    {
        var fullName = type.getFullyQualifiedName();
        if (fullName.isPresent() && (type.isPublic() || type.isProtected()) && !fullName.get().endsWith("Test"))
        {
            totalTypes++;

            var requiredLength = lexakai().get(lexakai().JAVADOC_TYPE_COMMENT_MINIMUM_LENGTH);
            var javadoc = type.getJavadoc();
            var isSignificant = type.toString().length() > lexakai().get(lexakai().JAVADOC_SIGNIFICANT_CLASS_MINIMUM_LENGTH);
            var significance = isSignificant ? "+" : "";
            var typeName = Strip.packagePrefix(fullName.get());
            var isCovered = true;
            var typeWarning = false;
            if (javadoc.isPresent())
            {
                if (!isJavadocComplete(type))
                {
                    if (type.isEnumDeclaration())
                    {
                        requiredLength = lexakai().get(lexakai().JAVADOC_ENUM_COMMENT_MINIMUM_LENGTH);
                    }
                    var text = javadoc.get().toText();
                    if (text.length() < requiredLength)
                    {
                        isCovered = false;
                        typeWarning = true;
                        warnings.add("${string}$: Javadoc coverage is only $ characters (minimum is $)",
                                significance, typeName, text.length(), requiredLength);
                    }
                }

                var methodWarnings = new StringList();
                for (var method : type.getMethods())
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

    public void addToVariableMap(VariableMap<String> variables)
    {
        variables.put("project-javadoc-coverage", projectCoverage() + ".  \n  \n&nbsp; &nbsp; " + meterMarkdown());
        var undocumented = significantUndocumentedClasses;
        variables.put("project-undocumented-classes",
                undocumented.isEmpty() ? "" : "The following significant classes are undocumented:  \n\n" +
                        undocumented.prefixedWith("- ").join("  \n"));
    }

    @Override
    public int compareTo(@NotNull JavadocCoverage that)
    {
        return projectCoverage().compareTo(that.projectCoverage());
    }

    public String coverage()
    {
        return Strings.format("$ ($ types, $ methods)", projectCoverage(), typeCoverage(), methodCoverage());
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

    public String projectCoverageMeter(Folder folder)
    {
        return "&nbsp; " + meterMarkdown() + " &nbsp; &nbsp; " + project.link(folder);
    }

    public StringList summary()
    {
        var list = new StringList();
        list.add(summaryCoverage());
        if (lexakai().get(lexakai().SHOW_JAVADOC_UNCOVERED_TYPES))
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

    private boolean isJavadocComplete(NodeWithAnnotations<?> node)
    {
        var optionalAnnotation = node.getAnnotationByClass(LexakaiJavadoc.class);
        return optionalAnnotation.filter(expr -> Annotations.booleanValue(expr, "complete", false)).isPresent();
    }

    private Lexakai lexakai()
    {
        return Lexakai.get();
    }

    private String meterMarkdown()
    {
        return project.meterMarkdownForPercent(projectCoverage());
    }

    private Percent methodCoverage()
    {
        compute();
        return methodCoverage;
    }

    private boolean methodCovered(MethodDeclaration method, StringList warnings)
    {
        if (!isJavadocComplete(method))
        {
            var lines = method.getBody().toString().split("\n").length;
            if (lines > lexakai().get(lexakai().JAVADOC_MINIMUM_METHOD_LINES))
            {
                var javadoc = method.getJavadoc();
                if (javadoc.isEmpty())
                {
                    warnings.add("    $(): Javadoc missing", method.getName());
                    return false;
                }
                else
                {
                    var length = javadoc.get().toText().length();
                    var minimumLength = lexakai().get(lexakai().JAVADOC_METHOD_COMMENT_MINIMUM_LENGTH);
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
        return Strings.format("$: $", Align.right(project.name(), 32, ' '), coverage());
    }

    private Percent typeCoverage()
    {
        compute();
        return typeCoverage;
    }
}
