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

package com.telenav.lexakai.builders;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.Type;
import com.telenav.kivakit.core.string.IndentingStringBuilder;
import com.telenav.kivakit.core.language.collections.list.StringList;
import com.telenav.kivakit.core.language.strings.Strings;
import com.telenav.lexakai.LexakaiClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.lexakai.associations.UmlAssociation;
import com.telenav.lexakai.associations.UmlInheritance;
import com.telenav.lexakai.library.Annotations;
import com.telenav.lexakai.library.Associations;
import com.telenav.lexakai.library.Fields;
import com.telenav.lexakai.library.Members;
import com.telenav.lexakai.library.Methods;
import com.telenav.lexakai.library.Names;
import com.telenav.lexakai.library.Types;

import java.util.ArrayList;

import static com.telenav.lexakai.associations.UmlAssociation.AssociationType.RELATION;
import static com.telenav.lexakai.library.Names.Qualification.QUALIFIED;
import static com.telenav.lexakai.library.Names.Qualification.UNQUALIFIED;
import static com.telenav.lexakai.library.Names.TypeParameters.WITHOUT_TYPE_PARAMETERS;
import static com.telenav.lexakai.library.Names.TypeParameters.WITH_TYPE_PARAMETERS;

/**
 * @author jonathanl (shibo)
 */
public class AssociationBuilder
{
    private final LexakaiClassDiagram diagram;

    private final TypeDeclaration<?> type;

    AssociationBuilder(LexakaiClassDiagram diagram, TypeDeclaration<?> type)
    {
        this.diagram = diagram;
        this.type = type;
    }

    void addExplicitRelations(IndentingStringBuilder builder)
    {
        for (var association : Associations.explicitRelations(type, diagram.name()))
        {
            if (association != null)
            {
                builder.appendLine(association.uml());
            }
        }
    }

    void addFieldAssociations(IndentingStringBuilder builder)
    {
        // For each field,
        var associations = new ArrayList<UmlAssociation>();
        type.getFields().forEach(field ->
        {
            var diagram = Members.associationString(field, "diagram");
            if (diagram == null || diagram.equals(this.diagram.name()))
            {
                // that isn't ignored or a non-reference,
                var fieldType = field.getCommonType();
                if (!Fields.isExcluded(field) && fieldType != null && Types.isReference(fieldType))
                {
                    // extract the type and type parameters,
                    var associationType = Fields.associationType(field);
                    if (associationType != null)
                    {
                        if (Types.isReference(fieldType))
                        {
                            // and add an association to the diagram.
                            var association = createAssociation(associationType, fieldType,
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

    void addInheritanceRelations(IndentingStringBuilder builder)
    {
        if (type.isClassOrInterfaceDeclaration() && !Annotations.shouldExcludeType(type))
        {
            // add type inheritance associations,
            var type = this.type.asClassOrInterfaceDeclaration();
            var qualifiedTypeName = Names.name(type, QUALIFIED, WITHOUT_TYPE_PARAMETERS);
            var associations = new ArrayList<UmlInheritance>();
            var interfaceDeclarations = new StringList();

            // extended types,
            type.getExtendedTypes().forEach(at ->
            {
                var superType = Names.name(at, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS);
                if (superType != null)
                {
                    var inheritance = new UmlInheritance(superType, qualifiedTypeName);
                    if (!diagram.has(inheritance) && !Types.isExcludedSuperType(this.type, diagram, superType))
                    {
                        diagram.add(inheritance);
                        associations.add(inheritance);
                        if (type.isInterface())
                        {
                            interfaceDeclarations.add("interface " + Names.name(at, UNQUALIFIED, WITH_TYPE_PARAMETERS));
                        }
                    }
                }
            });

            // implemented interfaces,
            type.getImplementedTypes().forEach(at ->
            {
                var superType = Names.name(at, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS);
                if (superType != null)
                {
                    var inheritance = new UmlInheritance(superType, qualifiedTypeName);
                    if (!diagram.has(inheritance) && !Types.isExcludedSuperType(this.type, diagram, superType))
                    {
                        diagram.add(inheritance);
                        associations.add(inheritance);
                    }
                }
            });

            // and add associations to the builder.
            interfaceDeclarations.sorted().forEach(builder::appendLine);
            associations.forEach(at -> builder.appendLine(at.uml()));
        }
    }

    void addMethodAssociations(IndentingStringBuilder builder)
    {
        // For each included method,
        diagram.includedMethods(type, method ->
        {
            var diagram = Members.associationString(method, "diagram");
            if (diagram == null || diagram.equals(this.diagram.name()))
            {
                // that isn't ignored or a non-reference,
                var returnType = method.getType();
                boolean associated = false;
                if (!Methods.isExcluded(method) && Types.isReference(returnType))
                {
                    // extract the type and type parameters,
                    var associationType = Methods.associationType(method);
                    if (associationType != null)
                    {
                        if (Types.isReference(returnType))
                        {
                            // and add an association to the UML.
                            var association = createAssociation(associationType, returnType,
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
                if (!associated && Names.simpleName(method).toLowerCase()
                        .matches("(build[A-Z]?\\w+|create[A-Z]\\w+|new[A-Z]\\w+)"))
                {
                    var association = createAssociation(
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
    private UmlAssociation createAssociation(UmlAssociation.AssociationType associationType,
                                             Type referentType,
                                             String explicitReferentType,
                                             String refereeCardinality,
                                             String referentCardinality,
                                             String label)
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
        var referent = diagram.referent(referentType);
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
