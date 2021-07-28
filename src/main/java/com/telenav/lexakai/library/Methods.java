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

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.telenav.lexakai.annotations.UmlMethodGroup;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeMember;
import com.telenav.lexakai.associations.UmlAssociation;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for working with {@link MethodDeclaration}s and {@link ConstructorDeclaration}s.
 *
 * @author jonathanl (shibo)
 */
public class Methods
{
    /**
     * @return The association type, if any, for the given method
     */
    public static UmlAssociation.AssociationType associationType(final MethodDeclaration method)
    {
        if (method.getAnnotationByClass(UmlRelation.class).isPresent())
        {
            return UmlAssociation.AssociationType.RELATION;
        }
        return null;
    }

    /**
     * @return The set of method groups that the given method belongs to
     */
    public static Set<String> explicitGroups(final MethodDeclaration method)
    {
        final var groups = new HashSet<String>();
        for (final var annotation : Annotations.annotations(method, UmlMethodGroup.class))
        {
            groups.add(Annotations.stringValue(annotation));
        }
        return groups;
    }

    /**
     * @return True if the given constructor is excluded from all diagrams
     */
    public static boolean isExcluded(final ConstructorDeclaration constructor)
    {
        return constructor.getAnnotationByClass(UmlExcludeMember.class).isPresent();
    }

    /**
     * @return True if the given method is excluded from all diagrams
     */
    public static boolean isExcluded(final MethodDeclaration method)
    {
        return method.getAnnotationByClass(UmlExcludeMember.class).isPresent();
    }
}
