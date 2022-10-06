package com.telenav.lexakai.javadoc;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.telenav.kivakit.annotations.code.CodeQuality;
import com.telenav.kivakit.annotations.code.DocumentationQuality;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.collections.map.VariableMap;
import com.telenav.kivakit.core.string.Align;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.value.level.Percent;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.lexakai.LexakaiProject;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.library.Annotations;
import org.jetbrains.annotations.NotNull;

import static com.telenav.kivakit.annotations.code.DocumentationQuality.DOCUMENTATION_COMPLETE;
import static com.telenav.kivakit.annotations.code.DocumentationQuality.DOCUMENTATION_INSUFFICIENT;
import static com.telenav.kivakit.annotations.code.DocumentationQuality.DOCUMENTATION_NONE;
import static com.telenav.kivakit.core.string.Split.split;

/**
 * @author jonathanl (shibo)
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class JavadocCoverage implements Comparable<JavadocCoverage>
{
    private final LexakaiProject project;

    private final StringList warnings = new StringList();

    private int totalTypes;

    private int totalCoveredTypes;

    private Percent typeCoverage;

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
            var typeName = fullName.get();
            var javadocQuality = javadocQuality(type);
            if (javadocQuality != DOCUMENTATION_COMPLETE)
            {
                warnings.add("$: Javadoc coverage is $", split(typeName, ".").last(), javadocQuality);
            }
            else
            {
                totalCoveredTypes++;
            }
        }
    }

    public void addToVariableMap(VariableMap<String> variables)
    {
        variables.put("project-javadoc-coverage", typeCoverage() + ".  \n  \n&nbsp; &nbsp; " + meterMarkdown());
    }

    @Override
    public int compareTo(@NotNull JavadocCoverage that)
    {
        return typeCoverage().compareTo(that.typeCoverage());
    }

    public String coverage()
    {
        return Strings.format("$ types", typeCoverage());
    }

    public String details()
    {
        return summary().join("\n");
    }

    public LexakaiProject project()
    {
        return project;
    }

    public String projectCoverageMeter(Folder folder)
    {
        return "&nbsp; " + meterMarkdown() + " &nbsp; &nbsp; " + project.link(folder);
    }

    public StringList summary()
    {
        var list = new StringList();
        list.add(summaryCoverage());
        return list;
    }

    @Override
    public String toString()
    {
        return coverage();
    }

    public Percent typeCoverage()
    {
        compute();
        return typeCoverage;
    }

    public StringList warnings()
    {
        return warnings;
    }

    private void compute()
    {
        if (typeCoverage == null)
        {
            typeCoverage = totalTypes == 0 ? Percent._0 : Percent.percent(100.0 * totalCoveredTypes / totalTypes);
        }
    }

    private DocumentationQuality javadocQuality(NodeWithAnnotations<?> node)
    {
        var apiQualityAnnotation = node.getAnnotationByClass(CodeQuality.class);
        if (apiQualityAnnotation.isPresent())
        {
            var apiQuality = apiQualityAnnotation.get();
            var documentation = Annotations.value(apiQuality, "documentation");
            if (documentation != null)
            {
                var name = documentation.asNameExpr().getName().asString();
                return DocumentationQuality.valueOf(name);
            }
        }
        var lexakaiJavadocAnnotation = node.getAnnotationByClass(LexakaiJavadoc.class);
        if (lexakaiJavadocAnnotation.isPresent())
        {
            var lexakaiJavadoc = lexakaiJavadocAnnotation.get();
            var complete = Annotations.booleanValue(lexakaiJavadoc, "complete", false);
            return complete ? DOCUMENTATION_COMPLETE : DOCUMENTATION_INSUFFICIENT;
        }
        return DOCUMENTATION_NONE;
    }

    private String meterMarkdown()
    {
        return project.meterMarkdownForPercent(typeCoverage());
    }

    private String summaryCoverage()
    {
        compute();
        return Strings.format("$: $", Align.right(project.name(), 32, ' '), coverage());
    }
}
