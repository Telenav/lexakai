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

package com.telenav.lexakai.associations;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.Type;
import com.telenav.lexakai.LexakaiClassDiagram;
import com.telenav.lexakai.library.Names;
import com.telenav.lexakai.library.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.telenav.lexakai.library.Names.Qualification.UNQUALIFIED;
import static com.telenav.lexakai.library.Names.TypeParameters.WITHOUT_TYPE_PARAMETERS;

/**
 * Represents a UML association (relation, aggregation or composition) arrow.
 *
 * @author jonathanl (shibo)
 */
public class UmlAssociation implements Comparable<UmlAssociation>
{
    /**
     * Creates a {@link UmlAssociation} that points from the referrer to the referent. The cardinality of the from and
     * to ends of the label can be specified with the "from" and "to" parameters, or they can be left null to leave the
     * cardinality unlabeled. The arrow itself can be labeled with the label parameter.
     *
     * @param referrer The from end of the arrow
     * @param type The kind of arrow (relation, aggregation or composition)
     * @param referent The to end of the arrow
     * @param from The cardinality of the from end of the arrow
     * @param to The cardinality of the to end of the arrow
     * @param label The arrow label, or null if there is none
     * @return The {@link UmlAssociation} to include in a {@link LexakaiClassDiagram}
     */
    public static UmlAssociation umlAssociation(TypeDeclaration<?> referrer,
                                                AssociationType type,
                                                Type referent,
                                                String from,
                                                String to,
                                                String label)
    {
        if (Types.isObject(referent))
        {
            return new UmlAssociation(referrer, type, Names.name(referent.asClassOrInterfaceType(), UNQUALIFIED, WITHOUT_TYPE_PARAMETERS), from, to, label);
        }
        return null;
    }

    public static UmlAssociation umlAssociation(TypeDeclaration<?> referrer,
                                                AssociationType type,
                                                String referent,
                                                String from,
                                                String to,
                                                String label)
    {
        return new UmlAssociation(referrer, type, referent, from, to, label);
    }

    /**
     * The type of association arrow
     */
    public enum AssociationType
    {
        /** An arbitrary kind of relation */
        RELATION("-->"),

        /** A non-dependent relation */
        AGGREGATION("o--"),

        /** A type-dependent relation */
        COMPOSITION("*--");

        private final String arrow;

        AssociationType(String arrow)
        {
            this.arrow = arrow;
        }

        @Override
        public String toString()
        {
            return arrow;
        }
    }

    /** The type of association */
    private final AssociationType type;

    /** The from end of the arrow */
    private final TypeDeclaration<?> referrer;

    /** The to end of the arrow */
    private final String referent;

    /** The cardinality of the from end of the arrow */
    private final String from;

    /** The cardinality of the to end of the arrow */
    private final String to;

    /** The arrow label */
    private final String label;

    private UmlAssociation(TypeDeclaration<?> referrer,
                           AssociationType type,
                           String referent,
                           String from,
                           String to,
                           String label)
    {
        this.type = type;
        this.referrer = referrer;
        this.referent = referent;
        this.from = from;
        this.to = to;
        this.label = label;
    }

    @Override
    public int compareTo(@NotNull UmlAssociation that)
    {
        return uml().compareTo(that.uml());
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof UmlAssociation)
        {
            UmlAssociation that = (UmlAssociation) object;
            return referrer.equals(that.referrer) && referent.equals(that.referent) && label.equals(that.label);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(referrer, referent, label);
    }

    @Override
    public String toString()
    {
        return uml();
    }

    public AssociationType type()
    {
        return type;
    }

    public String uml()
    {
        var builder = new StringBuilder();
        builder.append(Names.name(referrer, UNQUALIFIED, WITHOUT_TYPE_PARAMETERS));
        builder.append(" ");
        final String DOUBLE_QUOTES = "\"";
        if (from != null)
        {
            builder.append(DOUBLE_QUOTES)
                    .append(from)
                    .append(DOUBLE_QUOTES)
                    .append(' ');
        }
        builder.append(type);
        builder.append(" ");
        if (to != null)
        {
            builder.append(DOUBLE_QUOTES)
                    .append(to)
                    .append(DOUBLE_QUOTES)
                    .append(' ');
        }
        builder.append(Names.withoutTypeParameters(Names.withoutQualification(referent)));
        if (label != null)
        {
            builder.append(" : ")
                    .append(DOUBLE_QUOTES)
                    .append(label)
                    .append(DOUBLE_QUOTES);
        }
        return builder.toString();
    }
}
