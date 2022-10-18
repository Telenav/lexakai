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

import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.associations.UmlComposition;
import com.telenav.lexakai.annotations.associations.UmlRelation;

import java.lang.annotation.Annotation;

/**
 * @author jonathanl (shibo)
 */
public class Members
{
    /**
     * Returns the association string for the given method and member
     */
    public static String associationString(NodeWithAnnotations<?> member,
                                           Class<? extends Annotation> annotation,
                                           String key)
    {
        var expression = member.getAnnotationByClass(annotation);
        return expression.map(expr -> Annotations.stringValue(expr, key)).orElse(null);
    }

    /**
     * Returns any relation value for the given key on the given member
     */
    public static String associationString(NodeWithAnnotations<?> member, String key)
    {
        var aggregation = member.getAnnotationByClass(UmlAggregation.class);
        if (aggregation.isPresent())
        {
            return Annotations.stringValue(aggregation.get(), key);
        }

        var composition = member.getAnnotationByClass(UmlComposition.class);
        if (composition.isPresent())
        {
            return Annotations.stringValue(composition.get(), key);
        }

        var relation = member.getAnnotationByClass(UmlRelation.class);
        return relation.map(expr -> Annotations.stringValue(expr, key)).orElse(null);
    }
}
