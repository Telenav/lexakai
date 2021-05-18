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

import com.github.javaparser.ast.body.FieldDeclaration;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.associations.UmlComposition;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeMember;
import com.telenav.lexakai.associations.UmlAssociation;

/**
 * Utility methods for extracting information from {@link FieldDeclaration} elements.
 *
 * @author jonathanl (shibo)
 */
public class Fields
{
    /**
     * @return The association type for the given field
     */
    public static UmlAssociation.AssociationType associationType(final FieldDeclaration field)
    {
        if (field.getAnnotationByClass(UmlRelation.class).isPresent())
        {
            return UmlAssociation.AssociationType.RELATION;
        }
        if (field.getAnnotationByClass(UmlComposition.class).isPresent())
        {
            return UmlAssociation.AssociationType.COMPOSITION;
        }
        if (field.getAnnotationByClass(UmlAggregation.class).isPresent())
        {
            return UmlAssociation.AssociationType.AGGREGATION;
        }
        return null;
    }

    /**
     * @return True if the field is excluded from all diagrams
     */
    public static boolean isExcluded(final FieldDeclaration field)
    {
        return field.getAnnotationByClass(UmlExcludeMember.class).isPresent();
    }
}
