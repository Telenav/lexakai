package com.telenav.lexakai.quality;

import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.telenav.kivakit.annotations.code.CodeQuality;
import com.telenav.kivakit.annotations.code.CodeStability;
import com.telenav.kivakit.annotations.code.CodeType;
import com.telenav.kivakit.annotations.code.DocumentationQuality;
import com.telenav.kivakit.annotations.code.TestingQuality;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.string.KivaKitFormat;
import com.telenav.kivakit.core.string.ObjectFormatter;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.library.Annotations;

import static com.telenav.kivakit.annotations.code.CodeStability.CODE_UNEVALUATED;
import static com.telenav.kivakit.annotations.code.CodeType.CODE_PUBLIC;
import static com.telenav.kivakit.annotations.code.DocumentationQuality.DOCUMENTATION_COMPLETE;
import static com.telenav.kivakit.annotations.code.DocumentationQuality.DOCUMENTATION_INSUFFICIENT;
import static com.telenav.kivakit.annotations.code.DocumentationQuality.DOCUMENTATION_UNEVALUATED;

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
        quality.documentation = DOCUMENTATION_UNEVALUATED;
        quality.codeType = CODE_PUBLIC;
        quality.stability = CODE_UNEVALUATED;
        quality.testing = TestingQuality.TESTING_UNEVALUATED;

        var codeQualityAnnotation = node.getAnnotationByClass(CodeQuality.class);
        if (codeQualityAnnotation.isPresent())
        {
            var codeQuality = codeQualityAnnotation.get();

            var documentation = Annotations.value(codeQuality, "documentation");
            if (documentation != null)
            {
                var name = documentation.asNameExpr().getName().asString();
                quality.documentation = DocumentationQuality.valueOf(name);
            }

            var stability = Annotations.value(codeQuality, "stability");
            if (stability != null)
            {
                var name = stability.asNameExpr().getName().asString();
                quality.stability = CodeStability.valueOf(name);
            }

            var testing = Annotations.value(codeQuality, "testing");
            if (testing != null)
            {
                var name = testing.asNameExpr().getName().asString();
                quality.testing = TestingQuality.valueOf(name);
            }

            var codeType = Annotations.value(codeQuality, "type");
            if (codeType != null)
            {
                var name = codeType.asNameExpr().getName().asString();
                quality.codeType = CodeType.valueOf(name);
            }
        }

        var lexakaiJavadocAnnotation = node.getAnnotationByClass(LexakaiJavadoc.class);
        if (lexakaiJavadocAnnotation.isPresent())
        {
            var lexakaiJavadoc = lexakaiJavadocAnnotation.get();
            var complete = Annotations.booleanValue(lexakaiJavadoc, "complete", false);
            quality.documentation = complete ? DOCUMENTATION_COMPLETE : DOCUMENTATION_INSUFFICIENT;
        }

        return quality;
    }

    @KivaKitFormat
    String typeName;

    @KivaKitFormat
    DocumentationQuality documentation;

    @KivaKitFormat
    CodeStability stability;

    @KivaKitFormat
    TestingQuality testing;

    @KivaKitFormat
    CodeType codeType;

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
