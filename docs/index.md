## Lexakai &nbsp; ![](images/lexakai-64.png)

Lexakai is a command line tool used to create UML diagrams and markdown documentation indexes.

![](images/horizontal-line.png)

### Summary <a name = "summary"></a>

*Lexakai* - from lexis (greek for 'word') and kai (hawaiian for 'ocean').

Creates documentation indexes and UML diagrams from the source code of each maven or gradle
project discovered recursively from the root folder(s) given as argument(s).

### Download Executable JAR <a name = "download"></a>&nbsp; ![](images/down-arrow-32.png)

[Lexakai 0.9.2](tools/building/lexakai-0.9.2.jar)

### Creating Basic UML Package Diagrams <a name = "creating-basic-uml-package-diagrams"></a>&nbsp; ![](images/box-40.png)

Lexakai automatically creates basic package diagrams for all public types without further configuration.

### Custom UML Diagrams <a name = "custom-uml-diagrams"></a>&nbsp; &nbsp; ![](images/diagram-48.png)

To create a set of one or more custom diagrams for a project, annotations can be used from this project:

    <dependency>
        <groupId>com.telenav.lexakai</groupId>
        <artifactId>lexakai-annotations</artifactId>
        <version>${project-version}</version>
    </dependency>

A class, annotation, enum or interface annotated with *@UmlClassDiagram(diagram = [marker-interface].class)*,
where *[marker-interface]* extends the interface *UmlDiagramIdentifier*, will be included in the specified diagram.

For example, a type annotated with:

    @UmlClassDiagram(diagram = DiagramMapServer.class)

will be included in the diagram *diagram-map-server* (the lowercase, hyphenated name derived from
the marker interface name). A type can be used in more than one diagram, by specifying more than one
*@UmlClassDiagram* annotation:

    @UmlClassDiagram(diagram = DiagramMapServer.class)
    @UmlClassDiagram(diagram = DiagramServers.class)

The preferred location for diagram marker interfaces is in the sub-package *project.lexakai.diagrams*,
and they should be prefixed with *Diagram* to make their use clear.

### Grouping Methods in Class Diagrams <a name = "grouping-methods-in-class-diagrams"></a>&nbsp; ![](images/set-40.png)

Groups of methods can be specified using the *@UmlMethodGroup* annotation. Method groups will
be given labeled separators in class diagrams, making the list of methods easier to understand.

For example:

    @UmlMethodGroup("configuration")

could be used to label all the configuration-related methods in a type. Multiple *@UmlMethodGroup*
annotations can be added to specify that a method should be shown in more than one group.

### Automatic Method Groups <a name = "automatic-method-groups"></a>&nbsp;![](images/gears-40.png)

The *@UmlMethodGroup* annotation can be avoided if the switch *-automatic-method-groups* is set to *true*
(which is the default).  In this case, the best guess will be made based on method name and parameter
patterns as to which group a method most likely belongs. When the guess is inaccurate, or no guess is made,
an *@UmlMethodGroup* annotation can be applied to correct the result. The set of patterns that are used to
determine automatic groups is in a file called *lexakai.groups* in the documentation folder. This file can be
customized for a particular project.

### UML Associations <a name = "uml-associations"></a>&nbsp; ![](images/right-arrow-32.png)

Patterns in the names of types, fields and methods are used to try to deduce UML associations.
The annotation *@UmlRelation* can be used to override this guess with an explicit relation if the association
was not be deduced, or it was deduced incorrectly.

If the annotation *@UmlRelation(label = \"*label*\")* is applied to a method or field, a relation arrow will
be drawn from the enclosing type to the type of the member. Similar annotations are available for field
aggregation (*@UmlAggregation*) and composition (*@UmlComposition*). If the *@UmlRelation* tag is applied to a
type, it can specify a relation with a particular referent and cardinality. An explicit relation of this type
can be made specific to a particular diagram with the *diagram* parameter. Multiple *@UmlRelation* tags can
be applied to a type.

### Excluding Types And Members <a name = "excluding-types-and-members"></a>&nbsp;![](images/no-32.png)

Methods and fields can be excluded entirely by labeling them with *@UmlExcludeMember*.

### Non-Public Apis <a name = "non-public-apis"></a>

*@UmlNotPublicApi* marks a type or member as private even if it is not actually private.

### Annotation Summary <a name = "annotation-summary"></a>&nbsp; ![](images/annotation-32.png)

#### Diagrams

    @UmlClassDiagram - declares the diagram(s) that the annotated type should be included in
     @UmlMethodGroup - includes the annotated method in a labeled method group in the diagram
            @UmlNote - adds a callout note to a type or method

#### Visibility

    @UmlExcludeSuperTypes - excludes the listed supertypes from all diagrams
        @UmlExcludeMember - excludes the annotated member
        @UmlIncludeMember - includes the annotated member, even if it wouldn't normally be included
         @UmlNotPublicApi - marks the annotated type or member as private even if it is not

#### Associations

       @UmlRelation - adds a labeled UML relation from the enclosing type to the annotated member type
    @UmlAggregation - adds a UML aggregation association from the enclosing type to the annotated field type
    @UmlComposition - adds a UML composition association from the enclosing type to the annotated field type

### Configuration <a name = "configuration"></a>&nbsp; ![](images/tools-40.png)

The *documentation* folder for each project should contain a *lexakai.properties* file that looks similar to this:

    #
    # Project
    #
    project-name         = my-project
    project-description  = This is my project.
    project-javadoc-url  = http://myproject.mypna.com/com.telenav.my.project
    project-icon         = images/myproject.png
    project-footer       = Copyright by Me

    #
    # Diagram Titles
    #
    diagram-my-project   = My Project

The diagram name (the lowercase, hyphenated version of the marker interface) is used as a key to locate
the title of the diagram. For example:

    @UmlClassDiagram(diagram = DiagramMapService.class)

refers to the diagram title specified by the key *diagram-map-service* in the *lexakai.properties* file.

### Readme Generation and Updating <a name = "readme-generation-and-updating"></a>&nbsp; ![](images/pencil-32.png)

If the *-update-readme* switch is set to *true* (it is *false* by default to ensure it doesn't overwrite an
existing file) then a *README.md* file will be generated or updated each time the UML diagrams are generated.
This markdown file will use *project-name* as its title and insert the description *project-description*
from the *lexakai.properties* file as the project description. An index of project diagrams is updated along with
an index of any Javadoc at *project-javadoc-url* for all diagrammed types. Sections of documentation in the
Javadoc will also be indexed based on the pattern specified by the switch *-javadoc-section-pattern*.
By default, this pattern is:

    <p><b>section-title</b></p>

which is the style used in the KivaKit.

Any text between the markdown comments *start-user-text* and *end-user-text* will be preserved,
allowing additional documentation to be maintained.

![](images/horizontal-line.png)

<sub>© 2011-2021 [Telenav](http://telenav.com), Inc. Licensed under [Apache License, Version 2.0](../LICENSE)</sub>
<sub>This documentation was generated by Lexakai on 2021.04.02</sub>
<sub>UML diagrams courtesy of PlantUML (http://plantuml.com)</sub>

