////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2022 Telenav, Inc.
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

package com.telenav.lexakai;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.Type;
import com.telenav.kivakit.interfaces.naming.Named;
import com.telenav.kivakit.core.string.IndentingStringBuilder;
import com.telenav.kivakit.core.string.IndentingStringBuilder.Indentation;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.string.CaseFormat;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.string.Wrap;
import com.telenav.kivakit.resource.packages.PackagePath;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.lexakai.associations.UmlInheritance;
import com.telenav.lexakai.library.Annotations;
import com.telenav.lexakai.library.Associations;
import com.telenav.lexakai.library.Diagrams;
import com.telenav.lexakai.library.Names;
import com.telenav.lexakai.library.Types;
import com.telenav.lexakai.types.UmlType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.string.IndentingStringBuilder.Style.TEXT;
import static com.telenav.lexakai.library.Names.Qualification.QUALIFIED;
import static com.telenav.lexakai.library.Names.Qualification.UNQUALIFIED;
import static com.telenav.lexakai.library.Names.TypeParameters.WITHOUT_TYPE_PARAMETERS;
import static com.telenav.lexakai.library.Names.TypeParameters.WITH_TYPE_PARAMETERS;

/**
 * The UML diagram for a project.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings({ "ClassEscapesDefinedScope", "unused" })
public class LexakaiClassDiagram extends BaseLexakaiDiagram implements Named
{
    public static class Referent
    {
        final String cardinality;

        final Type referent;

        public Referent(String cardinality, Type referent)
        {
            this.cardinality = cardinality;
            this.referent = referent;
        }

        public String cardinality()
        {
            return cardinality;
        }

        public Type referent()
        {
            return referent;
        }
    }

    /** The set of abstract superclasses referenced by this diagram */
    private final HashSet<String> abstractSuperClasses = new HashSet<>();

    /** The set of types to include in this diagram */
    private final Map<String, UmlType> includedQualifiedTypes = new HashMap<>();

    /** Inheritance relations */
    private final Set<UmlInheritance> inheritances = new HashSet<>();

    /** The name of this diagram from @{@link LexakaiClassDiagram} annotations */
    private final String name;

    /** The project that this diagram belongs to */
    private final LexakaiProject project;

    /** The set super-interfaces referenced by this diagram */
    private final HashSet<Object> superInterfaces = new HashSet<>();

    /** The title of this diagram */
    private String title;

    public LexakaiClassDiagram(LexakaiProject project, String name)
    {
        ensure(!Strings.isNullOrBlank(name));

        this.project = project;
        this.name = name;
    }

    /**
     * Returns the set of abstract superclasses not in this project, based on naming conventions.
     */
    public HashSet<String> abstractSuperClasses()
    {
        // If we haven't found the abstract superclasses
        if (abstractSuperClasses.isEmpty())
        {
            // go through each type declaration in the project
            project.typeDeclarations(type ->
            {
                if (type.isClassOrInterfaceDeclaration() && !Annotations.shouldExcludeType(type))
                {
                    var classOrInterface = type.asClassOrInterfaceDeclaration();
                    if (includesQualifiedTypeName(Names.name(type, QUALIFIED, WITHOUT_TYPE_PARAMETERS)))
                    {
                        classOrInterface.getExtendedTypes().forEach(at ->
                        {
                            var superClass = at.getName().asString();
                            if (!Types.isExcludedSuperType(type, this, Names.name(at, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS)))
                            {
                                if (superClass.startsWith("Base") || superClass.startsWith("Abstract"))
                                {
                                    abstractSuperClasses.add(superClass);
                                }
                            }
                        });
                    }
                }
            });
        }

        return abstractSuperClasses;
    }

    /**
     * Adds the given inheritance relation to this diagram
     */
    public boolean add(UmlInheritance inheritance)
    {
        return inheritances.add(inheritance);
    }

    public boolean automaticMethodGroups()
    {
        return project.automaticMethodGroups();
    }

    /**
     * Returns true if this diagram already has the given inheritance relation
     */
    public boolean has(UmlInheritance inheritance)
    {
        return inheritances.contains(inheritance);
    }

    public String identifier()
    {
        var name = name();

        // If the diagram name is all lowercase, it is a package name,
        if (PackagePath.isPackagePath(name))
        {
            // so just return it
            return name;
        }

        // otherwise, convert to hyphenated identifier (DiagramStuff -> diagram-stuff)
        return CaseFormat.camelCaseToHyphenated(Names.withoutQualification(name));
    }

    /**
     * Includes the given type in this diagram
     */
    @SuppressWarnings("ClassEscapesDefinedScope")
    public void include(UmlType type)
    {
        includedQualifiedTypes.put(type.name(QUALIFIED, WITHOUT_TYPE_PARAMETERS), type);
    }

    /**
     * Returns true if the given type includes members in this diagram
     */
    public boolean includeMembers(TypeDeclaration<?> type)
    {
        var expression = Diagrams.diagramAnnotation(type, name());
        var includeMembers = expression != null && Annotations.booleanValue(expression, "includeMembers", true);
        return includeMembers || (isPackageDiagram() && project().buildPackageDiagrams());
    }

    public boolean includeOverrides(TypeDeclaration<?> type)
    {
        var diagram = Diagrams.diagramAnnotation(type, name());
        return diagram != null && Annotations.booleanValue(diagram, "includeOverrides", false);
    }

    public List<MethodDeclaration> includedMethods(TypeDeclaration<?> type)
    {
        var methods = new ArrayList<MethodDeclaration>();
        includedMethods(type, methods::add);
        return methods;
    }

    /**
     * Calls the consumer with each method in this type that is included in the diagram we are building
     */
    public void includedMethods(TypeDeclaration<?> type, Consumer<MethodDeclaration> consumer)
    {
        type.getMethods().forEach(method ->
        {
            boolean include = Types.isInterface(type)
                    || method.isPublic()
                    || (method.isProtected() && project().includeProtectedMethods() && typeIncludesProtectedMethods(type));

            if (!project().includeObjectMethods())
            {
                var methodName = method.getName().asString();
                if ("hashCode".equals(methodName) || "equals".equals(methodName) || "toString".equals(methodName))
                {
                    include = false;
                }
            }

            if (method.getAnnotationByClass(Override.class).isPresent())
            {
                if (method.getAnnotationByClass(UmlRelation.class).isPresent())
                {
                    include = true;
                }
                else
                {
                    include = includeOverrides(type);
                }
            }

            if (include)
            {
                consumer.accept(method);
            }
        });
    }

    public List<UmlType> includedQualifiedTypes()
    {
        var sorted = new ArrayList<>(includedQualifiedTypes.values());
        sorted.sort(Comparator.comparing(type -> type.name(UNQUALIFIED, WITHOUT_TYPE_PARAMETERS)));
        return sorted;
    }

    /**
     * Returns true if this diagram included the given type name
     */
    public boolean includesQualifiedTypeName(String typeName)
    {
        return includedQualifiedTypes.containsKey(Names.withoutTypeParameters(typeName));
    }

    /**
     * Returns true if this diagram has no types in it
     */
    public boolean isEmpty()
    {
        return includedQualifiedTypes.isEmpty();
    }

    public boolean isPackageDiagram()
    {
        return PackagePath.isPackagePath(name());
    }

    @Override
    public String name()
    {
        return name;
    }

    @Override
    public void onUml(IndentingStringBuilder builder)
    {
        // add type declarations for any abstract superclasses that are not in the project
        // (otherwise they will be shown as normal classes in the diagram),
        abstractSuperClasses().forEach(at -> builder.appendLine("abstract " + at));
        if (abstractSuperClasses().size() > 0)
        {
            builder.appendLine("");
        }

        // and also interfaces external to the project,
        superInterfaces().forEach(at -> builder.appendLine("interface " + at));
        if (superInterfaces().size() > 0)
        {
            builder.appendLine("");
        }

        // then for each type declaration in the project,
        var added = new StringList();
        project.typeDeclarations(type ->
        {
            // if this diagram should include the given type,
            var umlType = includedQualifiedTypes.get(Names.name(type, QUALIFIED, WITHOUT_TYPE_PARAMETERS));
            if (umlType != null)
            {
                // and there is UML for it,
                var uml = umlType.uml();
                if (uml != null)
                {
                    // add its UML.
                    added.add(umlType.name(UNQUALIFIED, WITH_TYPE_PARAMETERS));
                    builder.appendLines(uml);
                }
            }
        });
        var indenter = new IndentingStringBuilder(TEXT, Indentation.indentation(8));
        indenter.indent();
        indenter.appendLines(Wrap.wrap(added.join(", "), 80));
    }

    /**
     * Returns the project that this diagram belongs to
     */
    public LexakaiProject project()
    {
        return project;
    }

    /**
     * Returns the full names of all types in this diagram
     */
    public Set<String> qualifiedTypeNames()
    {
        return includedQualifiedTypes.keySet();
    }

    /**
     * Returns the type of the referent if it is included in the diagram or if it is not, the first type argument that
     * is included in the diagram.
     */
    public Referent referent(Type referent)
    {
        // If the type is included in the diagram,
        if (includesQualifiedTypeName(Names.name(referent, QUALIFIED, WITHOUT_TYPE_PARAMETERS)))
        {
            // return the referent itself
            return new Referent("1", referent);
        }
        else
        {
            // otherwise go through the relevant types of the referent (including type arguments),
            for (var at : Types.typeParameters(referent))
            {
                // and if the type is part of our diagram,
                if (includesQualifiedTypeName(Names.name(at, QUALIFIED, WITH_TYPE_PARAMETERS)))
                {
                    // then create a referent with the type parameter and a cardinality guess on the referent,
                    // for example, the referent might be List and the type parameter might be Switch, in which
                    // case the cardinality would be *
                    return new Referent(Associations.cardinalityGuess(referent), at);
                }
            }

            // There is no type in the diagram in the referent, so guess its cardinality
            var cardinality = Associations.cardinalityGuess(referent);

            // get its type parameters
            var typeParameters = Types.typeParameters(referent);

            // and if the cardinality is * and there are type parameters,
            if ("*".equals(cardinality) && !typeParameters.isEmpty())
            {
                // then refer to the first type parameters,
                return new Referent(cardinality, typeParameters.get(0));
            }
            else
            {
                // otherwise just refer to the referent.
                return new Referent(cardinality, referent);
            }
        }
    }

    /**
     * Returns the set of super-interfaces used that are not in this project
     */
    public HashSet<Object> superInterfaces()
    {
        // If we haven't found the super-interfaces yet
        if (superInterfaces.isEmpty())
        {
            // go through each type declaration in the project
            project.typeDeclarations(type ->
            {
                if (type.isClassOrInterfaceDeclaration() && !Annotations.shouldExcludeType(type))
                {
                    var classOrInterface = type.asClassOrInterfaceDeclaration();
                    if (includesQualifiedTypeName(Names.name(type, QUALIFIED, WITH_TYPE_PARAMETERS)))
                    {
                        classOrInterface.getImplementedTypes().forEach(at ->
                        {
                            var superInterface = at.getName().asString();
                            if (!Types.isExcludedSuperType(type, this, Names.name(at, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS)))
                            {
                                superInterfaces.add(superInterface);
                            }
                        });
                    }
                }
            });
        }
        return superInterfaces;
    }

    public String title()
    {
        if (title == null)
        {
            title = project.property(identifier());
            if (title == null)
            {
                title = identifier();
                if (!isPackageDiagram())
                {
                    var lexakai = Lexakai.get();
                    if (lexakai.get(lexakai.SHOW_DIAGRAM_WARNINGS))
                    {
                        warning("    No title found for diagram '$'", identifier());
                    }
                }
            }
        }
        return title;
    }

    private boolean typeIncludesProtectedMethods(TypeDeclaration<?> type)
    {
        var umlDiagramAnnotation = type.getAnnotationByClass(UmlClassDiagram.class);
        return umlDiagramAnnotation.map(annotation -> Annotations.booleanValue(annotation, "includeProtectedMethods", true)).orElse(true);
    }
}
