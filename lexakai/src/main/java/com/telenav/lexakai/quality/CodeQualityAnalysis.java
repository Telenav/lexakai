package com.telenav.lexakai.quality;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.collections.map.VariableMap;
import com.telenav.kivakit.core.value.level.Percent;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.lexakai.LexakaiProject;
import org.jetbrains.annotations.NotNull;

import static com.telenav.kivakit.core.string.Align.rightAlign;
import static com.telenav.kivakit.core.string.Formatter.format;
import static com.telenav.kivakit.core.string.Split.split;
import static com.telenav.kivakit.core.value.level.Percent.percent;

/**
 * @author jonathanl (shibo)
 */
@SuppressWarnings({ "BooleanMethodIsAlwaysInverted", "unused" })
public class CodeQualityAnalysis implements Comparable<CodeQualityAnalysis>
{
    private final LexakaiProject project;

    private final StringList warnings = new StringList();

    private int totalTypes;

    private int javadocCoveredTypes;

    private int stableTypes;

    private int testedTypes;

    public CodeQualityAnalysis(LexakaiProject project)
    {
        this.project = project;
    }

    public void add(TypeDeclaration<?> type)
    {
        var fullName = type.getFullyQualifiedName();
        if (fullName.isPresent() && (type.isPublic() || type.isProtected()) && !fullName.get().endsWith("Test"))
        {
            var typeName = fullName.get();
            var quality = CodeQualityOfType.quality(typeName, type);

            boolean warning = false;

            if (quality.audience.isPublic())
            {
                if (quality.documentation.isComplete())
                {
                    javadocCoveredTypes++;
                }
                else
                {
                    warning = true;
                }

                if (quality.stability.isStable())
                {
                    stableTypes++;
                }
                else
                {
                    warning = true;
                }

                if (quality.testing.isTested())
                {
                    testedTypes++;
                }
                else
                {
                    warning = true;
                }
            }

            if (warning)
            {
                warnings.add("Code quality of $: $", split(typeName, "\\.").last(), quality.problems());
            }

            totalTypes++;
        }
    }

    public void addToVariableMap(VariableMap<String> variables)
    {
        variables.put("project-quality", qualityEstimate() + ".  \n  \n&nbsp; &nbsp; " + qualityMeterMarkdown());
        variables.put("project-testing", percentTestedTypes() + "&nbsp; &nbsp; " + testingMeterMarkdown());
        variables.put("project-documentation", percentJavadocCovered() + "&nbsp; &nbsp; " + javadocMeterMarkdown());
        variables.put("project-stability", percentStableTypes() + "&nbsp; &nbsp; " + stabilityMeterMarkdown());
    }

    @Override
    public int compareTo(@NotNull CodeQualityAnalysis that)
    {
        return qualityEstimate().compareTo(that.qualityEstimate());
    }

    public String details()
    {
        return summary().join("\n");
    }

    public LexakaiProject project()
    {
        return project;
    }

    public String projectQualityMeter(Folder folder)
    {
        return "&nbsp; " + qualityMeterMarkdown() + " &nbsp; &nbsp; " + project.link(folder);
    }

    public Percent qualityEstimate()
    {
        return percentJavadocCovered()
                .plus(percentTestedTypes())
                .plus(percentStableTypes())
                .dividedBy(3);
    }

    public StringList summary()
    {
        var list = new StringList();
        list.add(projectTypes());
        return list;
    }

    @Override
    public String toString()
    {
        return totalTypes();
    }

    public String totalTypes()
    {
        return format("$ types", totalTypes);
    }

    public StringList warnings()
    {
        return warnings;
    }

    private String javadocMeterMarkdown()
    {
        return project.meterMarkdownForPercent(percentJavadocCovered());
    }

    private Percent percentJavadocCovered()
    {
        return totalTypes == 0 ? Percent._0 : percent(100.0 * javadocCoveredTypes / totalTypes);
    }

    private Percent percentStableTypes()
    {
        return totalTypes == 0 ? Percent._0 : percent(100.0 * stableTypes / totalTypes);
    }

    private Percent percentTestedTypes()
    {
        return totalTypes == 0 ? Percent._0 : percent(100.0 * testedTypes / totalTypes);
    }

    private String projectTypes()
    {
        return format("$: $", rightAlign(project.name(), 32, ' '), totalTypes());
    }

    private String qualityMeterMarkdown()
    {
        return project.meterMarkdownForPercent(qualityEstimate());
    }

    private String stabilityMeterMarkdown()
    {
        return project.meterMarkdownForPercent(percentStableTypes());
    }

    private String testingMeterMarkdown()
    {
        return project.meterMarkdownForPercent(percentTestedTypes());
    }
}
