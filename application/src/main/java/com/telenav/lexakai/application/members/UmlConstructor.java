package com.telenav.lexakai.application.members;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.telenav.lexakai.application.library.Methods;
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
