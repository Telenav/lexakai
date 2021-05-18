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
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.strings.CaseFormat;
import com.telenav.kivakit.kernel.language.strings.Strings;

import static com.telenav.lexakai.library.Names.Qualification.UNQUALIFIED;
import static com.telenav.lexakai.library.Names.TypeParameters.WITHOUT_TYPE_PARAMETERS;

/**
 * This class contains utility methods for working with JavaParser names. Names can be produced with or without package
 * qualification and with or without type parameters. The package name for a type can be deduced with {@link
 * #packageName(String)}.
 *
 * @author jonathanl (shibo)
 */
public class Names
{
    /**
     * @return The name of the given type with or without qualification or type parameters
     */
    public static String name(final Type type,
                              final Qualification qualification,
                              final TypeParameters parameters)
    {
        // If the type is a class or interface,
        if (type.isClassOrInterfaceType())
        {
            // get the qualified name using the scope recursively,
            final var classOrInterface = type.asClassOrInterfaceType();
            if (classOrInterface.getScope().isPresent())
            {
                return qualifiedName(classOrInterface);
            }
        }

        // otherwise try resolving the type to some type in the set of projects that the parser knows about,
        final var resolved = resolve(type);
        if (resolved != null)
        {
            // and return that if it's found,
            return name(resolved, qualification, parameters);
        }

        // and finally, use an unqualified simple name if we can't figure anything out
        return apply(simpleName(type), qualification, parameters);
    }

    /**
     * @return The name of the given type declaration with or without qualification or type parameters
     */
    public static String name(final TypeDeclaration<?> type,
                              final Qualification qualification,
                              final TypeParameters parameters)
    {
        // Get the fully qualified type name,
        final var qualifiedName = type.getFullyQualifiedName();

        // but just return the simple name if we can't do that.
        final var name = qualifiedName.isPresent() ? qualifiedName.get() : simpleName(type);
        final var typeParameters = typeParameters(type);
        return apply(name + Strings.notNull(typeParameters), qualification, parameters);
    }

    /**
     * @return The name of the given class expression with or without qualification or type parameters
     */
    public static String name(final ClassExpr expression,
                              final Qualification qualification,
                              final TypeParameters parameters)
    {
        if (qualification == UNQUALIFIED)
        {
            return expression.getType().asString();
        }

        final var resolved = resolve(expression.getType());
        if (resolved != null)
        {
            return name(resolved, qualification, parameters);
        }

        return expression.getType().asString();
    }

    public static String packageName(final String qualifiedName)
    {
        return qualifiedName.substring(0, qualifiedName.length() - withoutQualification(qualifiedName).length() - 1);
    }

    public static String simpleName(final Type node)
    {
        return node.asString();
    }

    public static String simpleName(final NodeWithSimpleName<?> node)
    {
        return node.getName().asString();
    }

    /**
     * @return Any type parameters of the given type as a string
     */
    public static String typeParameters(final TypeDeclaration<?> declaration)
    {
        if (declaration.isClassOrInterfaceDeclaration())
        {
            final var parameters = new StringList();
            for (final var parameter : declaration.asClassOrInterfaceDeclaration().getTypeParameters())
            {
                parameters.append(parameter.getName().asString());
            }
            return parameters.isEmpty() ? "" : "<" + parameters.join(", ") + ">";
        }
        return null;
    }

    /**
     * @return The given qualified class name without the package qualifier
     */
    public static String withoutQualification(final String qualifiedClassName)
    {
        final var components = StringList.split(qualifiedClassName, ".");
        while (!components.isEmpty() && !CaseFormat.isCapitalized(components.get(0)))
        {
            components.remove(0);
        }
        return components.join(".");
    }

    /**
     * @return The given type name without type parameters
     */
    public static String withoutTypeParameters(final String typeName)
    {
        return typeName.replaceAll("<.*>", "");
    }

    /**
     * Specifies whether types should be package-qualified or not.
     *
     * @author jonathanl (shibo)
     */
    public enum Qualification
    {
        QUALIFIED,
        UNQUALIFIED
    }

    /**
     * Specifies whether names should have type parameters or not.
     *
     * @author jonathanl (shibo)
     */
    public enum TypeParameters
    {
        WITH_TYPE_PARAMETERS,
        WITHOUT_TYPE_PARAMETERS
    }

    private static String apply(String name, final Qualification qualification, final TypeParameters parameters)
    {
        // Un-qualify the name if desired,
        if (qualification == UNQUALIFIED)
        {
            name = withoutQualification(name);
        }

        // and remove type parameters if desired,
        if (parameters == WITHOUT_TYPE_PARAMETERS)
        {
            name = withoutTypeParameters(name);
        }

        return name;
    }

    private static String name(final ResolvedReferenceType referenceType,
                               final Qualification qualification,
                               final TypeParameters parameters)
    {
        final var name = referenceType.asReferenceType().getQualifiedName();
        return apply(name, qualification, parameters);
    }

    private static String qualifiedName(final ClassOrInterfaceType classOrInterface)
    {
        String name = "";
        if (classOrInterface.getScope().isPresent())
        {
            name += qualifiedName(classOrInterface.getScope().get()) + ".";
        }
        return name + classOrInterface.getNameAsString();
    }

    private static ResolvedReferenceType resolve(final Type type)
    {
        try
        {
            final var resolved = type.resolve();
            if (resolved.isReferenceType())
            {
                return resolved.asReferenceType();
            }
        }
        catch (final Exception ignored)
        {
        }
        return null;
    }
}
