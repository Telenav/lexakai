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

package com.telenav.lexakai.library;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.telenav.kivakit.annotations.code.CodeQuality;
import com.telenav.lexakai.annotations.UmlClassDiagram;

import java.util.HashSet;
import java.util.Set;

import static com.telenav.kivakit.annotations.code.CodeStability.CODE_STABLE;
import static com.telenav.kivakit.annotations.code.DocumentationQuality.DOCUMENTATION_COMPLETE;
import static com.telenav.kivakit.annotations.code.TestingQuality.TESTING_NOT_NEEDED;

/**
 * @author jonathanl (shibo)
 */
@CodeQuality(stability = CODE_STABLE,
             testing = TESTING_NOT_NEEDED,
             documentation = DOCUMENTATION_COMPLETE)
public interface Diagrams
{
    /**
     * @return The @UmlClassDiagram annotation with the given lowercase hyphenated diagram name
     */
    public static AnnotationExpr diagramAnnotation(TypeDeclaration<?> type, String diagramName)
    {
        for (var annotation : Annotations.annotations(type, UmlClassDiagram.class))
        {
            var expression = Annotations.value(annotation, "diagram");
            if (expression != null)
            {
                if (diagramName.equals(Names.name(expression.asClassExpr(), Names.Qualification.UNQUALIFIED, Names.TypeParameters.WITHOUT_TYPE_PARAMETERS)))
                {
                    return annotation;
                }
            }
        }
        return null;
    }

    /**
     * @return The set of diagrams that the given type belongs to
     */
    public static Set<String> diagrams(TypeDeclaration<?> type, boolean includePackageDiagram)
    {
        var diagrams = new HashSet<String>();
        for (var annotation : Annotations.annotations(type, UmlClassDiagram.class))
        {
            var expression = Annotations.value(annotation, "diagram");
            if (expression != null)
            {
                var name = Names.name(expression.asClassExpr(), Names.Qualification.UNQUALIFIED, Names.TypeParameters.WITHOUT_TYPE_PARAMETERS);
                if (name != null)
                {
                    diagrams.add(name);
                }
            }
        }

        if (includePackageDiagram)
        {
            var qualifiedName = type.getFullyQualifiedName();
            qualifiedName.ifPresent(it -> diagrams.add(Names.packageName(it)));
        }
        return diagrams;
    }
}
