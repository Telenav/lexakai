package com.telenav.lexakai.builders;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.telenav.kivakit.core.kernel.language.strings.formatting.IndentingStringBuilder;
import com.telenav.lexakai.LexakaiClassDiagram;
import com.telenav.lexakai.library.Annotations;
import com.telenav.lexakai.library.Name;
import com.telenav.lexakai.library.Types;
import com.telenav.lexakai.annotations.UmlNote;

/**
 * @author jonathanl (shibo)
 */
public class TypeBuilder
{
    private final LexakaiClassDiagram diagram;

    private final TypeDeclaration<?> type;

    public TypeBuilder(final LexakaiClassDiagram diagram, final TypeDeclaration<?> type)
    {
        this.diagram = diagram;
        this.type = type;
    }

    /**
     * Adds the type declaration for this type to the given builder
     */
    public void addTypeDeclaration(final IndentingStringBuilder builder)
    {
        // Add inheritance and explicit relations,
        final var associationBuilder = new AssociationBuilder(diagram, type);
        associationBuilder.addInheritanceRelations(builder);
        associationBuilder.addExplicitRelations(builder);

        // and this type includes members,
        if (diagram.includeMembers(type))
        {
            // add method and field associations
            associationBuilder.addMethodAssociations(builder);
            associationBuilder.addFieldAssociations(builder);
        }

        // then, add the full UML type declaration.
        final var typeName = Name.of(type.asClassOrInterfaceDeclaration(), Name.Qualification.UNQUALIFIED, Name.TypeParameters.WITH_TYPE_PARAMETERS);
        addNote(builder, type, typeName);
        for (final var method : type.getMethods())
        {
            addNote(builder, method, typeName + "::" + Name.simpleName(method));
        }
        builder.appendLine(Types.typeDeclarationModifiers(type) + " " + typeName);
    }

    private void addNote(final IndentingStringBuilder builder, final NodeWithAnnotations<?> node, final String element)
    {
        final var note = node.getAnnotationByClass(UmlNote.class);
        if (note.isPresent())
        {
            var alignment = Annotations.stringValue(note.get(), "align");
            if (alignment == null)
            {
                alignment = "right";
            }
            builder.appendLine("note " + alignment.toLowerCase() + " of " + element + "\n" +
                    "    " + Annotations.stringValue(note.get(), "text") + "\n" +
                    "endnote");
            Annotations.stringValue(note.get(), "text");
        }
    }
}
