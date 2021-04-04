package com.telenav.lexakai.application.library;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.Type;
import com.telenav.lexakai.application.associations.UmlAssociation;
import com.telenav.lexakai.annotations.associations.UmlRelation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jonathanl (shibo)
 */
public class Associations
{
    /**
     * @return A guess as to the cardinality of the given type based on the type's name
     */
    public static String cardinalityGuess(final Type type)
    {
        if (type.isArrayType())
        {
            return "*";
        }
        if (Types.hasTypeParameters(type))
        {
            final var name = Name.of(type, Name.Qualification.UNQUALIFIED, Name.TypeParameters.WITHOUT_TYPE_PARAMETERS);
            if (name != null)
            {
                if (name.endsWith("List")
                        || name.endsWith("Set")
                        || name.endsWith("Map")
                        || name.endsWith("Collection"))
                {
                    return "*";
                }
            }
        }
        return "1";
    }

    /**
     * @return The set of UML associations defined by the given type with @UmlRelation annotations
     */
    public static List<UmlAssociation> explicitRelations(final TypeDeclaration<?> type, final String diagramName)
    {
        // Go through each annotation,
        final var relations = new ArrayList<UmlAssociation>();
        for (final var annotation : Annotations.annotationsOfType(type, UmlRelation.class))
        {
            // then loop through the key value pairs,
            final var relation = relation(diagramName, type, annotation);
            if (relation != null)
            {
                relations.add(relation);
            }
        }

        final var diagramAnnotation = Diagrams.diagramAnnotation(type, diagramName);
        if (diagramAnnotation != null)
        {
            final var relation = relation(diagramName, type, diagramAnnotation);
            if (relation != null)
            {
                relations.add(relation);
            }
        }

        Collections.sort(relations);
        return relations;
    }

    /**
     * @return The UML relation association for the given annotation from the given type in the given diagram (only).
     */
    private static UmlAssociation relation(final String diagramName,
                                           final TypeDeclaration<?> type,
                                           final AnnotationExpr annotation)
    {
        final var label = Annotations.stringValue(annotation, "label");
        final var referent = Annotations.className(annotation, "referent");
        final var diagram = Annotations.className(annotation, "diagram");
        final var refereeCardinality = Annotations.stringValue(annotation, "refereeCardinality");
        final var referentCardinality = Annotations.stringValue(annotation, "referentCardinality");

        // If the diagram specified is not the given diagram,
        if (diagram != null && !diagram.equals(diagramName))
        {
            // then this annotation doesn't apply,
            return null;
        }
        else
        {
            // otherwise, if we have both a label and a referent,
            if (label != null && referent != null)
            {
                // then return the relation.
                return UmlAssociation.of(type, UmlAssociation.AssociationType.RELATION, referent, refereeCardinality, referentCardinality, label);
            }
        }
        return null;
    }
}
