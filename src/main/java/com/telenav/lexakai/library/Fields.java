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
