package com.telenav.lexakai.quality;

import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.telenav.kivakit.annotations.code.quality.TypeQuality;
import com.telenav.kivakit.annotations.code.quality.Stability;
import com.telenav.kivakit.annotations.code.quality.Audience;
import com.telenav.kivakit.annotations.code.quality.Documentation;
import com.telenav.kivakit.annotations.code.quality.Testing;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.string.FormatProperty;
import com.telenav.kivakit.core.string.ObjectFormatter;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.library.Annotations;

import static com.telenav.kivakit.annotations.code.quality.Stability.STABILITY_UNDETERMINED;
import static com.telenav.kivakit.annotations.code.quality.Audience.AUDIENCE_PUBLIC;
import static com.telenav.kivakit.annotations.code.quality.Documentation.DOCUMENTED;
import static com.telenav.kivakit.annotations.code.quality.Documentation.DOCUMENTATION_INSUFFICIENT;
import static com.telenav.kivakit.annotations.code.quality.Documentation.DOCUMENTATION_UNDETERMINED;
import static com.telenav.kivakit.annotations.code.quality.Testing.TESTING_UNDETERMINED;

@SuppressWarnings("unused")
public class CodeQualityOfType
{
    /**
     * Analyzes the code quality for a single type
     *
     * @param typeName The type
     * @param node The JavaParser node to extract data from
     * @return The code quality, as described by annotations
     */
    public static CodeQualityOfType quality(String typeName, NodeWithAnnotations<?> node)
    {
        var quality = new CodeQualityOfType();
        quality.typeName = typeName;
        quality.documentation = DOCUMENTATION_UNDETERMINED;
        quality.audience = AUDIENCE_PUBLIC;
        quality.stability = STABILITY_UNDETERMINED;
        quality.testing = TESTING_UNDETERMINED;

        var codeQualityAnnotation = node.getAnnotationByClass(TypeQuality.class);
        if (codeQualityAnnotation.isPresent())
        {
            var codeQuality = codeQualityAnnotation.get();

            var documentation = Annotations.value(codeQuality, "documentation");
            if (documentation != null)
            {
                var name = documentation.asNameExpr().getName().asString();
                quality.documentation = Documentation.valueOf(name);
            }

            var stability = Annotations.value(codeQuality, "stability");
            if (stability != null)
            {
                var name = stability.asNameExpr().getName().asString();
                quality.stability = Stability.valueOf(name);
            }

            var testing = Annotations.value(codeQuality, "testing");
            if (testing != null)
            {
                var name = testing.asNameExpr().getName().asString();
                quality.testing = Testing.valueOf(name);
            }

            var codeType = Annotations.value(codeQuality, "type");
            if (codeType != null)
            {
                var name = codeType.asNameExpr().getName().asString();
                quality.audience = Audience.valueOf(name);
            }
        }

        var lexakaiJavadocAnnotation = node.getAnnotationByClass(LexakaiJavadoc.class);
        if (lexakaiJavadocAnnotation.isPresent())
        {
            var lexakaiJavadoc = lexakaiJavadocAnnotation.get();
            var complete = Annotations.booleanValue(lexakaiJavadoc, "complete", false);
            quality.documentation = complete ? DOCUMENTED : DOCUMENTATION_INSUFFICIENT;
        }

        return quality;
    }

    @FormatProperty
    String typeName;

    @FormatProperty
    Documentation documentation;

    @FormatProperty
    Stability stability;

    @FormatProperty
    Testing testing;

    @FormatProperty
    Audience audience;

    public String problems()
    {
        var problems = new StringList();
        if (documentation.isIncomplete())
        {
            problems.add(documentation.name());
        }
        if (stability.isUnstable())
        {
            problems.add(stability.name());
        }
        if (testing.isUntested())
        {
            problems.add(testing.name());
        }
        return problems.join();
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }
}
