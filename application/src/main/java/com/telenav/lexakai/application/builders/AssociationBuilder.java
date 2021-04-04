package com.telenav.lexakai.application.builders;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.Type;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.strings.Strings;
import com.telenav.kivakit.core.kernel.language.strings.formatting.IndentingStringBuilder;
import com.telenav.lexakai.application.LexakaiClassDiagram;
import com.telenav.lexakai.application.associations.UmlAssociation;
import com.telenav.lexakai.application.associations.UmlInheritance;
import com.telenav.lexakai.application.library.Associations;
import com.telenav.lexakai.application.library.Fields;
import com.telenav.lexakai.application.library.Members;
import com.telenav.lexakai.application.library.Methods;
import com.telenav.lexakai.application.library.Name;
import com.telenav.lexakai.application.library.Types;
import com.telenav.lexakai.annotations.associations.UmlRelation;

import java.util.ArrayList;

import static com.telenav.lexakai.application.associations.UmlAssociation.AssociationType.RELATION;
import static com.telenav.lexakai.application.library.Name.Qualification.QUALIFIED;
import static com.telenav.lexakai.application.library.Name.Qualification.UNQUALIFIED;
import static com.telenav.lexakai.application.library.Name.TypeParameters.WITHOUT_TYPE_PARAMETERS;
import static com.telenav.lexakai.application.library.Name.TypeParameters.WITH_TYPE_PARAMETERS;

/**
 * @author jonathanl (shibo)
 */
public class AssociationBuilder
{
    private final LexakaiClassDiagram diagram;

    private final TypeDeclaration<?> type;

    AssociationBuilder(final LexakaiClassDiagram diagram, final TypeDeclaration<?> type)
    {
        this.diagram = diagram;
        this.type = type;
    }

    void addExplicitRelations(final IndentingStringBuilder builder)
    {
        for (final var association : Associations.explicitRelations(type, diagram.name()))
        {
            if (association != null)
            {
                builder.appendLine(association.uml());
            }
        }
    }

    void addFieldAssociations(final IndentingStringBuilder builder)
    {
        // For each field,
        final var associations = new ArrayList<UmlAssociation>();
        type.getFields().forEach(field ->
        {
            final var diagram = Members.associationString(field, "diagram");
            if (diagram == null || diagram.equals(this.diagram.name()))
            {
                // that isn't ignored or a non-reference,
                final var fieldType = field.getCommonType();
                if (!Fields.isExcluded(field) && fieldType != null && Types.isReference(fieldType))
                {
                    // extract the type and type parameters,
                    final var associationType = Fields.associationType(field);
                    if (associationType != null)
                    {
                        if (Types.isReference(fieldType))
                        {
                            // and add an association to the diagram.
                            final var association = createAssociation(associationType, fieldType,
                                    Members.associationString(field, "referent"),
                                    Members.associationString(field, "refereeCardinality"),
                                    Members.associationString(field, "referentCardinality"),
                                    Members.associationString(field, "label"));

                            if (association != null)
                            {
                                associations.add(association);
                            }
                        }
                    }
                }
            }
        });

        associations.forEach(at -> builder.appendLine(at.uml()));
    }

    void addInheritanceRelations(final IndentingStringBuilder builder)
    {
        if (type.isClassOrInterfaceDeclaration())
        {
            // add type inheritance associations,
            final var type = this.type.asClassOrInterfaceDeclaration();
            final var qualifiedTypeName = Name.of(type, QUALIFIED, WITHOUT_TYPE_PARAMETERS);
            final var associations = new ArrayList<UmlInheritance>();
            final var interfaceDeclarations = new StringList();
            type.getExtendedTypes().forEach(at ->
            {
                final var superType = Name.of(at, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS);
                if (superType != null)
                {
                    final var inheritance = new UmlInheritance(superType, qualifiedTypeName);
                    if (!diagram.has(inheritance) && !Types.isExcludedSuperType(this.type, diagram, superType))
                    {
                        diagram.add(inheritance);
                        associations.add(inheritance);
                        if (type.isInterface())
                        {
                            interfaceDeclarations.add("interface " + Name.of(at, UNQUALIFIED, WITH_TYPE_PARAMETERS));
                        }
                    }
                }
            });

            // implemented interfaces,
            type.getImplementedTypes().forEach(at ->
            {
                final var superType = Name.of(at, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS);
                if (superType != null)
                {
                    final var inheritance = new UmlInheritance(superType, qualifiedTypeName);
                    if (!diagram.has(inheritance) && !Types.isExcludedSuperType(this.type, diagram, superType))
                    {
                        diagram.add(inheritance);
                        associations.add(inheritance);
                    }
                }
            });
            interfaceDeclarations.sorted().forEach(builder::appendLine);
            associations.forEach(at -> builder.appendLine(at.uml()));
        }
    }

    void addMethodAssociations(final IndentingStringBuilder builder)
    {
        // For each included method,
        diagram.includedMethods(type, method ->
        {
            final var diagram = Members.associationString(method, "diagram");
            if (diagram == null || diagram.equals(this.diagram.name()))
            {
                // that isn't ignored or a non-reference,
                final var returnType = method.getType();
                boolean associated = false;
                if (!Methods.isExcluded(method) && Types.isReference(returnType))
                {
                    // extract the type and type parameters,
                    final var associationType = Methods.associationType(method);
                    if (associationType != null)
                    {
                        if (Types.isReference(returnType))
                        {
                            // and add an association to the UML.
                            final var association = createAssociation(associationType, returnType,
                                    Members.associationString(method, UmlRelation.class, "referent"),
                                    Members.associationString(method, UmlRelation.class, "refereeCardinality"),
                                    Members.associationString(method, UmlRelation.class, "referentCardinality"),
                                    Members.associationString(method, UmlRelation.class, "label"));

                            if (association != null)
                            {
                                builder.appendLine(association.uml());
                                associated = true;
                            }
                        }
                    }
                }

                // If we did not find an association, try to deduce one
                if (!associated && Name.simpleName(method).toLowerCase()
                        .matches("(build[A-Z]?\\w+|create[A-Z]\\w+|new[A-Z]\\w+)"))
                {
                    final var association = createAssociation(
                            RELATION,
                            returnType,
                            Members.associationString(method, UmlRelation.class, "referent"),
                            "1",
                            "1",
                            "creates");

                    if (association != null)
                    {
                        builder.appendLine(association.uml());
                    }
                }
            }
        });
    }

    /**
     * @return Creates a {@link UmlAssociation} of the given type pointing to the given referent with the given label to
     * this type's diagram.
     */
    private UmlAssociation createAssociation(final UmlAssociation.AssociationType associationType,
                                             final Type referentType,
                                             final String explicitReferentType,
                                             String refereeCardinality,
                                             String referentCardinality,
                                             final String label)
    {
        // Assume 1 for any missing referee cardinality,
        refereeCardinality = Strings.isEmpty(refereeCardinality) ? "1" : refereeCardinality;

        // and if an explicit referent type was provided by the annotation,
        if (explicitReferentType != null)
        {
            // assume 1 for any missing referent cardinality,
            referentCardinality = Strings.isEmpty(referentCardinality) ? "1" : referentCardinality;

            // eliminate any 1:1 cardinalities,
            if ("1".equals(refereeCardinality) && "1".equals(referentCardinality))
            {
                // don't specify any cardinality to keep the diagram clean,
                refereeCardinality = null;
                referentCardinality = null;
            }

            // and return the association.
            return UmlAssociation.of(type, associationType, explicitReferentType, refereeCardinality, referentCardinality, label);
        }

        // Resolve the referent cardinality and type,
        final var referent = diagram.referent(referentType);
        if (referent != null)
        {
            // and if there is no explicit cardinality, use the deduced cardinality,
            referentCardinality = Strings.isEmpty(referentCardinality) ? referent.cardinality() : referentCardinality;

            // eliminate any 1:1 cardinalities,
            if ("1".equals(refereeCardinality) && "1".equals(referentCardinality))
            {
                refereeCardinality = null;
                referentCardinality = null;
            }

            // and return the association.
            return UmlAssociation.of(type, associationType, referent.referent(), refereeCardinality, referentCardinality, label);
        }
        return null;
    }
}