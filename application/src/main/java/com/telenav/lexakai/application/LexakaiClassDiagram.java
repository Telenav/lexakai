package com.telenav.lexakai.application;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.Type;
import com.telenav.kivakit.core.kernel.interfaces.naming.Named;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.paths.PackagePath;
import com.telenav.kivakit.core.kernel.language.strings.CaseFormat;
import com.telenav.kivakit.core.kernel.language.strings.Strings;
import com.telenav.kivakit.core.kernel.language.strings.Wrap;
import com.telenav.kivakit.core.kernel.language.strings.formatting.IndentingStringBuilder;
import com.telenav.kivakit.core.kernel.language.strings.formatting.IndentingStringBuilder.Indentation;
import com.telenav.lexakai.application.associations.UmlInheritance;
import com.telenav.lexakai.application.library.Annotations;
import com.telenav.lexakai.application.library.Associations;
import com.telenav.lexakai.application.library.Diagrams;
import com.telenav.lexakai.application.library.Name;
import com.telenav.lexakai.application.library.Types;
import com.telenav.lexakai.application.types.UmlType;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.kernel.language.strings.formatting.IndentingStringBuilder.Style.TEXT;
import static com.telenav.lexakai.application.library.Name.Qualification.QUALIFIED;
import static com.telenav.lexakai.application.library.Name.Qualification.UNQUALIFIED;
import static com.telenav.lexakai.application.library.Name.TypeParameters.WITHOUT_TYPE_PARAMETERS;
import static com.telenav.lexakai.application.library.Name.TypeParameters.WITH_TYPE_PARAMETERS;

/**
 * The UML diagram for a project.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("ClassEscapesDefinedScope")
public class LexakaiClassDiagram extends BaseLexakaiDiagram implements Named
{
    public static class Referent
    {
        final String cardinality;

        final Type referent;

        public Referent(final String cardinality, final Type referent)
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

    /** The name of this diagram from @{@link LexakaiClassDiagram} annotations */
    private final String name;

    /** The project that this diagram belongs to */
    private final LexakaiProject project;

    /** The set of types to include in this diagram */
    private final Map<String, UmlType> includedQualifiedTypes = new HashMap<>();

    /** Inheritance relations */
    private final Set<UmlInheritance> inheritances = new HashSet<>();

    /** The set of abstract superclasses referenced by this diagram */
    private final HashSet<String> abstractSuperClasses = new HashSet<>();

    /** The set super-interfaces referenced by this diagram */
    private final HashSet<Object> superInterfaces = new HashSet<>();

    /** The title of this diagram */
    private String title;

    public LexakaiClassDiagram(final LexakaiProject project, final String name)
    {
        ensure(!Strings.isEmpty(name));

        this.project = project;
        this.name = name;
    }

    /**
     * @return The set of abstract superclasses not in this project, based on naming conventions.
     */
    public HashSet<String> abstractSuperClasses()
    {
        // If we haven't found the abstract superclasses
        if (abstractSuperClasses.isEmpty())
        {
            // go through each type declaration in the project
            project.typeDeclarations(type ->
            {
                if (type.isClassOrInterfaceDeclaration())
                {
                    final var classOrInterface = type.asClassOrInterfaceDeclaration();
                    if (includesQualifiedTypeName(Name.of(type, QUALIFIED, WITHOUT_TYPE_PARAMETERS)))
                    {
                        classOrInterface.getExtendedTypes().forEach(at ->
                        {
                            final var superClass = at.getName().asString();
                            if (!Types.isExcludedSuperType(type, this, Name.of(at, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS)))
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
    public boolean add(final UmlInheritance inheritance)
    {
        return inheritances.add(inheritance);
    }

    public boolean automaticMethodGroups()
    {
        return project.automaticMethodGroups();
    }

    /**
     * @return True if this diagram already has the given inheritance relation
     */
    public boolean has(final UmlInheritance inheritance)
    {
        return inheritances.contains(inheritance);
    }

    public String identifier()
    {
        final var name = name();

        // If the diagram name is all lowercase, it is a package name,
        if (PackagePath.isPackagePath(name))
        {
            // so just return it
            return name;
        }

        // otherwise, convert to hyphenated identifier (DiagramStuff -> diagram-stuff)
        return CaseFormat.camelCaseToHyphenated(Name.withoutQualification(name));
    }

    /**
     * Includes the given type in this diagram
     */
    public void include(final UmlType type)
    {
        includedQualifiedTypes.put(type.name(QUALIFIED, WITHOUT_TYPE_PARAMETERS), type);
    }

    /**
     * @return True if the given type includes members in this diagram
     */
    public boolean includeMembers(final TypeDeclaration<?> type)
    {
        final var expression = Diagrams.diagramAnnotation(type, name());
        final var includeMembers = expression != null && Annotations.booleanValue(expression, "includeMembers", true);
        return includeMembers || (isPackageDiagram() && project().buildPackageDiagrams());
    }

    public boolean includeOverrides(final TypeDeclaration<?> type)
    {
        final var diagram = Diagrams.diagramAnnotation(type, name());
        return diagram != null && Annotations.booleanValue(diagram, "includeOverrides", false);
    }

    public List<MethodDeclaration> includedMethods(final TypeDeclaration<?> type)
    {
        final var methods = new ArrayList<MethodDeclaration>();
        includedMethods(type, methods::add);
        return methods;
    }

    /**
     * Calls the consumer with each method in this type that is included in the diagram we are building
     */
    public void includedMethods(final TypeDeclaration<?> type, final Consumer<MethodDeclaration> consumer)
    {
        type.getMethods().forEach(method ->
        {
            boolean include = Types.isInterface(type)
                    || method.isPublic()
                    || (method.isProtected() && project().includeProtectedMethods() && typeIncludesProtectedMethods(type));

            if (!project().includeObjectMethods())
            {
                final var methodName = method.getName().asString();
                if (methodName.equals("hashCode") || methodName.equals("equals") || methodName.equals("toString"))
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
        final var sorted = new ArrayList<>(includedQualifiedTypes.values());
        sorted.sort(Comparator.comparing(type -> type.name(UNQUALIFIED, WITHOUT_TYPE_PARAMETERS)));
        return sorted;
    }

    /**
     * @return True if this diagram included the given type name
     */
    public boolean includesQualifiedTypeName(final String typeName)
    {
        return includedQualifiedTypes.containsKey(Name.withoutTypeParameters(typeName));
    }

    /**
     * @return True if this diagram has no types in it
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
    public void onUml(final IndentingStringBuilder builder)
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
        final var added = new StringList();
        project.typeDeclarations(type ->
        {
            // if this diagram should include the given type,
            final var umlType = includedQualifiedTypes.get(Name.of(type, QUALIFIED, WITHOUT_TYPE_PARAMETERS));
            if (umlType != null)
            {
                // add its UML.
                added.add(umlType.name(UNQUALIFIED, WITH_TYPE_PARAMETERS));
                builder.appendLines(umlType.uml());
            }
        });
        final var indenter = new IndentingStringBuilder(TEXT, Indentation.of(8));
        indenter.indent();
        indenter.appendLines(Wrap.wrap(added.join(", "), 80));
        announce(indenter.toString());
    }

    /**
     * @return The project that this diagram belongs to
     */
    public LexakaiProject project()
    {
        return project;
    }

    /**
     * @return The full names of all types in this diagram
     */
    public Set<String> qualifiedTypeNames()
    {
        return includedQualifiedTypes.keySet();
    }

    /**
     * @return The type of the referent if it is included in the diagram or if it is not, the first type argument that
     * is included in the diagram.
     */
    public Referent referent(final Type referent)
    {
        // If the type is included in the diagram,
        if (includesQualifiedTypeName(Name.of(referent, QUALIFIED, WITHOUT_TYPE_PARAMETERS)))
        {
            // return the referent itself
            return new Referent("1", referent);
        }
        else
        {
            // otherwise go through the relevant types of the referent (including type arguments),
            for (final var at : Types.typeParameters(referent))
            {
                // and if the type is part of our diagram,
                if (includesQualifiedTypeName(Name.of(at, QUALIFIED, WITH_TYPE_PARAMETERS)))
                {
                    // then create a referent with the type parameter and a cardinality guess on the referent,
                    // for example, the referent might be List and the type parameter might be Switch, in which
                    // case the cardinality would be *
                    return new Referent(Associations.cardinalityGuess(referent), at);
                }
            }

            // There is no type in the diagram in the referent, so guess its cardinality
            final var cardinality = Associations.cardinalityGuess(referent);

            // get its type parameters
            final var typeParameters = Types.typeParameters(referent);

            // and if the cardinality is * and there are type parameters,
            if (cardinality.equals("*") && !typeParameters.isEmpty())
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
     * @return The set of super-interfaces used that are not in this project
     */
    public HashSet<Object> superInterfaces()
    {
        // If we haven't found the super-interfaces yet
        if (superInterfaces.isEmpty())
        {
            // go through each type declaration in the project
            project.typeDeclarations(type ->
            {
                if (type.isClassOrInterfaceDeclaration())
                {
                    final var classOrInterface = type.asClassOrInterfaceDeclaration();
                    if (includesQualifiedTypeName(Name.of(type, QUALIFIED, WITH_TYPE_PARAMETERS)))
                    {
                        classOrInterface.getImplementedTypes().forEach(at ->
                        {
                            final var superInterface = at.getName().asString();
                            if (!Types.isExcludedSuperType(type, this, Name.of(at, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS)))
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
                    warning("No title found for diagram: $", identifier());
                }
            }
        }
        return title;
    }

    private boolean typeIncludesProtectedMethods(final TypeDeclaration<?> type)
    {
        final var umlDiagramAnnotation = type.getAnnotationByClass(UmlClassDiagram.class);
        if (umlDiagramAnnotation.isPresent())
        {
            return Annotations.booleanValue(umlDiagramAnnotation.get(), "includeProtectedMethods", true);
        }
        return true;
    }
}