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

package com.telenav.lexakai.builders.grouper;

import com.telenav.kivakit.collections.map.MultiMap;
import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.strings.CaseFormat;
import com.telenav.kivakit.kernel.language.strings.Strings;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.resource.resources.packaged.PackageResource;
import com.telenav.lexakai.Lexakai;
import com.telenav.lexakai.LexakaiProject;
import com.telenav.lexakai.library.Names;
import com.telenav.lexakai.library.Types;
import com.telenav.lexakai.members.UmlMethod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * @author jonathanl (shibo)
 */
public class MethodGroupNameGuesser
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    // [<word>+] or [<word>+ = <parameter>]
    private static final Pattern MACRO_PATTERN = Pattern.compile("(?x) \\[ (?<name> \\w+ ( - \\w+ )*) \\s* ( = \\s* (?<parameter> .*?))? \\s* ]");

    /** Patterns and the group names that they select */
    private final Map<Pattern, String> groupNamePatterns = new LinkedHashMap<>();

    private final LexakaiProject project;

    public MethodGroupNameGuesser(final LexakaiProject project)
    {
        this.project = project;

        load();
    }

    public Set<String> groupNames(final UmlMethod method)
    {
        final var parameterNames = new StringList();
        for (final var parameter : method.method().getParameters())
        {
            parameterNames.add(CaseFormat.camelCaseToHyphenated(parameter.getType().asString() + " " + parameter.getName().asString()));
        }

        final var returnType = method.method().getType();
        String returnTypeName = "";
        if (Types.isReference(returnType))
        {
            returnTypeName = Names.name(returnType, Names.Qualification.UNQUALIFIED, Names.TypeParameters.WITH_TYPE_PARAMETERS) + " ";
        }
        final var name = CaseFormat.camelCaseToHyphenated(returnTypeName + method.simpleName()) + "(" + parameterNames.join(",") + ")";

        final var names = new HashSet<String>();
        for (final var pattern : groupNamePatterns.keySet())
        {
            final var groupName = groupNamePatterns.get(pattern);
            if (pattern.matcher(name).matches())
            {
                names.add("(" + groupName + ")");
            }
        }

        if (names.isEmpty())
        {
            names.add("none");
        }

        return names;
    }

    private void add(final String groupName, final String pattern, final int flags)
    {
        groupNamePatterns.put(Pattern.compile(pattern, flags), groupName);
    }

    private void add(final String groupName, final List<String> patterns, final Map<String, String> macros)
    {
        final var words = new StringList();
        final var regularExpressions = new StringList();

        // Go through the patterns that select the given group name,
        for (final var pattern : patterns)
        {
            // and if the pattern is a bare word,
            if (Strings.isJavaIdentifier(pattern))
            {
                // add it to the list of words in hyphenated form (getIdentifier -> get-identifier),
                words.add(CaseFormat.camelCaseToHyphenated(pattern));
            }
            else
            {
                // otherwise, we have a regular expression so resolve any pattern macros
                final var matcher = MACRO_PATTERN.matcher(pattern);
                final var regularExpression = matcher.replaceAll(match ->
                {
                    // by getting the name and parameter
                    final var macroName = matcher.group("name");
                    final var parameter = matcher.group("parameter");

                    // retrieving the named macro
                    var macro = macros.get(macroName);
                    if (macro == null)
                    {
                        LOGGER.warning("Unrecognized macro '$'", macroName);
                        macro = "";
                    }
                    else if (macro.contains("#"))
                    {
                        if (parameter == null)
                        {
                            LOGGER.warning("Missing parameter to '$' macro", macroName);
                        }
                        else
                        {
                            // and substituting the parameter if necessary.
                            macro = macro.replaceAll("#", parameter);
                        }
                    }

                    return " ( " + Strings.replaceAll(macro, "\\", "\\\\") + " ) ";
                });

                // then add the expression to the list.
                regularExpressions.add(regularExpression);
            }
        }

        if (!words.isEmpty())
        {
            regularExpressions.add("( .* \\b (" + words.join("|") + ") \\b .* \\( .* \\) )");
        }

        add(groupName, "(?x) " + regularExpressions.join("|"), CASE_INSENSITIVE);
    }

    private void load(final Resource resource)
    {
        String groupName = null;
        String macroName = null;
        final var groups = new MultiMap<String, String>();
        final var macros = new HashMap<String, String>();
        for (final var line : resource.reader().lines())
        {
            if (Strings.isEmpty(line.trim()) || line.startsWith("//"))
            {
                continue;
            }
            if (line.startsWith("  "))
            {
                if (macroName != null)
                {
                    macros.put(macroName, line.trim());
                }
                else
                {
                    groups.add(groupName, line.trim());
                }
            }
            else if (line.startsWith("group"))
            {
                groupName = line.replaceAll("group\\s*=\\s*", "").trim();
                macroName = null;
            }
            else if (line.startsWith("pattern"))
            {
                macroName = line.replaceAll("pattern\\s*=\\s*", "").trim();
                groupName = null;
            }
        }

        for (final var group : groups.keySet())
        {
            add(group, groups.list(group), macros);
        }
    }

    private void load()
    {
        final var groupsFile = project.files().lexakaiGroups();
        if (groupsFile.exists())
        {
            load(groupsFile);
        }
        else
        {
            load(PackageResource.of(Lexakai.class, "lexakai/lexakai.groups"));
        }
    }
}
