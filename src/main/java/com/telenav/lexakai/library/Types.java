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
import com.github.javaparser.ast.type.Type;
import com.telenav.lexakai.LexakaiClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.lexakai.annotations.visibility.UmlNotPublicApi;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;

/**
 * Utilities for working with JavaParser types.
 *
 * @author jonathanl (shibo)
 */
public class Types
{
    /**
     * The set of types that we completely ignore
     */
    private static final Set<String> excludedTypes = Set.of(
            "Boolean",
            "Byte",
            "Character",
            "Short",
            "Integer",
            "Long",
            "Float",
            "Double",
            "String"
    );

    /**
     * @return True if the given type has generic type parameters
     */
    public static boolean hasTypeParameters(Type type)
    {
        if (type.isClassOrInterfaceType())
        {
            var arguments = type.asClassOrInterfaceType().getTypeArguments();
            return arguments.isPresent() && !arguments.get().isEmpty();
        }
        return false;
    }

    /**
     * @return True if the given supertype is excluded by a @UmlExcludeSuperTypes annotation
     */
    public static boolean isExcludedSuperType(TypeDeclaration<?> type,
                                              LexakaiClassDiagram diagram,
                                              String supertype)
    {
        if (supertype != null)
        {
            var annotation = type.getAnnotationByClass(UmlExcludeSuperTypes.class);
            if (annotation.isPresent())
            {
                if (annotation.get().isMarkerAnnotationExpr())
                {
                    return true;
                }
                var classes = Annotations.classNames(annotation.get());
                if (classes.contains(supertype))
                {
                    return true;
                }
            }

            var diagramAnnotation = Diagrams.diagramAnnotation(type, diagram.name());
            if (diagramAnnotation != null)
            {
                var excludeAll = Annotations.booleanValue(diagramAnnotation.asAnnotationExpr(), "excludeAllSuperTypes", false);
                return excludeAll || Annotations.classNames(diagramAnnotation, "excludeSuperTypes").contains(supertype);
            }
        }
        return false;
    }

    /**
     * @return True if the given type is an interface
     */
    public static boolean isInterface(TypeDeclaration<?> type)
    {
        return type.isClassOrInterfaceDeclaration() && type.asClassOrInterfaceDeclaration().isInterface();
    }

    /**
     * @return True if the given type has a @UmlNotPublicApi annotation
     */
    public static boolean isNotPublicApi(TypeDeclaration<?> type)
    {
        return type.getAnnotationByClass(UmlNotPublicApi.class).isPresent();
    }

    /**
     * @return True if the given type is a reference to an object, but not an array.
     */
    public static boolean isObject(Type type)
    {
        return type.isReferenceType() && !type.isArrayType();
    }

    /**
     * @return True if the given type is a valid reference type. Unknown types, wildcards, primitive types and the
     * built-in list of excluded types are not considered valid references.
     */
    public static boolean isReference(Type type)
    {
        if (!type.isUnknownType() && !type.isWildcardType() && !type.isPrimitiveType())
        {
            var name = Names.name(type, Names.Qualification.UNQUALIFIED, Names.TypeParameters.WITHOUT_TYPE_PARAMETERS);
            return name != null && !excludedTypes.contains(name);
        }
        return false;
    }

    /**
     * @return The type of the declaration: interface, abstract class, class, enum or annotation.
     */
    public static String type(TypeDeclaration<?> type)
    {
        if (type.isClassOrInterfaceDeclaration())
        {
            var classOrInterface = type.asClassOrInterfaceDeclaration();
            if (isInterface(type))
            {
                return "interface";
            }
            else
            {
                if (classOrInterface.isAbstract())
                {
                    return "abstract class";
                }
                else
                {
                    return "class";
                }
            }
        }
        if (type.isEnumDeclaration())
        {
            return "enum";
        }
        if (type.isAnnotationDeclaration())
        {
            return "annotation";
        }

        return fail("Unknown type $", type);
    }

    /**
     * @return The leading modifiers to a type name, for example, "-abstract class"
     */
    public static String typeDeclarationModifiers(TypeDeclaration<?> type)
    {
        String modifiers = Types.type(type);
        return (Types.isNotPublicApi(type) ? "-" : "") + modifiers;
    }

    /**
     * @return Returns the parameters to the given type. The element type of an array is considered a type argument.
     */
    public static List<Type> typeParameters(Type type)
    {
        if (isObject(type))
        {
            if (type.isArrayType())
            {
                return List.of(type.getElementType());
            }
            else
            {
                if (type.isClassOrInterfaceType())
                {
                    var typeArguments = type.asClassOrInterfaceType().getTypeArguments();
                    if (typeArguments.isPresent())
                    {
                        return typeArguments.get().stream()
                                .filter(Types::isReference)
                                .collect(Collectors.toList());
                    }
                }
            }
        }
        return List.of();
    }
}
