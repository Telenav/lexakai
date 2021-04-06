////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.lexakai.members;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.telenav.lexakai.library.Methods;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a UML constructor method declaration.
 *
 * @author jonathanl (shibo)
 */
public class UmlConstructor implements Comparable<UmlConstructor>
{
    /** The constructor declaration */
    private final ConstructorDeclaration constructor;

    public UmlConstructor(final ConstructorDeclaration constructor)
    {
        this.constructor = constructor;
    }

    @Override
    public int compareTo(@NotNull final UmlConstructor that)
    {
        return constructor.getDeclarationAsString().compareTo(that.constructor.getDeclarationAsString());
    }

    public boolean isExcluded()
    {
        return Methods.isExcluded(constructor);
    }

    public boolean isProtected()
    {
        return constructor.isPublic();
    }

    public boolean isPublic()
    {
        return constructor.isPublic();
    }

    public String name()
    {
        return constructor.getName().asString();
    }

    public String uml()
    {
        return (isPublic() ? "+" : "#") + constructor.getDeclarationAsString(false, false, false);
    }
}
