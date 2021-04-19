# Lexakai &nbsp;&nbsp; <img src="https://www.lexakai.org/images/lexakai-64.png" srcset="https://www.lexakai.org/images/lexakai-64-2x.png 2x"/>

A tool for generating UML diagrams and markdown documentation indexes.

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512@2x.png 2x"/>

### Index

[**Summary**](#summary)  
[**Example**](#example)  
[**Download Executable JAR**](#download-executable-jar)  
[**Creating Basic UML Package Diagrams**](#creating-basic-uml-package-diagrams)  
[**Custom UML Diagrams**](#custom-uml-diagrams)  
[**Grouping Methods in Class Diagrams**](#grouping-methods-in-class-diagrams)  
[**Automatic Method Groups**](#automatic-method-groups)  
[**UML Associations**](#uml-associations)  
[**Excluding Types And Members**](#excluding-types-and-members)  
[**Non-Public Apis**](#non-public-apis)  
[**Annotation Summary**](#annotation-summary)  
[**Configuration**](#configuration)  
[**Targeting Output to a Different Repository**](#targeting-output-to-a-different-repository)  
[**Readme Generation and Updating**](#readme-generation-and-updating)  
[**Custom README Templates**](#custom-readme-templates)  

[**Dependencies**](#dependencies) | [**Class Diagrams**](#class-diagrams) | [**Package Diagrams**](#package-diagrams) | [**Javadoc**](#javadoc)

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512@2x.png 2x"/>

### Dependencies <a name="dependencies"></a> &nbsp;&nbsp; <img src="https://www.lexakai.org/images/dependencies-32.png" srcset="https://www.lexakai.org/images/dependencies-32-2x.png 2x"/>

[*Dependency Diagram*](https://www.lexakai.org/lexakai/lexakai/documentation/diagrams/dependencies.svg)

#### Maven Dependency

    <dependency>
        <groupId>com.telenav.lexakai</groupId>
        <artifactId>lexakai</artifactId>
        <version>0.9.4</version>
    </dependency>


<img src="https://www.kivakit.org/images/short-horizontal-line-128.png" srcset="https://www.kivakit.org/images/short-horizontal-line-128@2x.png 2x"/>

[//]: # (start-user-text)

### Summary <a name = "summary"></a>

*Lexakai* - from lexis (greek for 'word') and kai (hawaiian for 'ocean').

Creates documentation indexes and UML diagrams from the source code of each maven or gradle
project discovered recursively from the root folder(s) given as argument(s).

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Example <a name = "example"></a>

This documentation was created by Lexakai, including:

- Section index
- Maven dependency
- Dependency diagram
- Class diagrams
- Package diagrams
- Javadoc coverage

For another example of Lexakai documentation, see [KivaKit](https://github.com/Telenav/kivakit).

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Download Executable JAR <a name = "download"></a>&nbsp; <img src="https://www.kivakit.org/images/down-arrow-32.png" srcset="https://www.kivakit.org/images/down-arrow-32-2x.png 2x"/>

[Lexakai 0.9.4](https://www.lexakai.org/builds/lexakai-0.9.4.jar)

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Creating Basic UML Package Diagrams <a name = "creating-basic-uml-package-diagrams"></a>&nbsp; <img src="https://www.kivakit.org/images/box-32.png" srcset="https://www.kivakit.org/images/box-32-2x.png 2x"/>

Lexakai automatically creates basic package diagrams for all public types without further configuration.

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Custom UML Diagrams <a name = "custom-uml-diagrams"></a>&nbsp; &nbsp; <img src="https://www.kivakit.org/images/diagram-32.png" srcset="https://www.kivakit.org/images/diagram-32-2x.png 2x"/>

To create a set of one or more custom diagrams for a project, annotations can be used from this project:

    <dependency>
        <groupId>com.telenav.lexakai</groupId>
        <artifactId>lexakai-annotations</artifactId>
        <version>0.9.3</version>
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

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Grouping Methods in Class Diagrams <a name = "grouping-methods-in-class-diagrams"></a>&nbsp; <img src="https://www.kivakit.org/images/set-32.png" srcset="https://www.kivakit.org/images/set-32-2x.png 2x"/>

Groups of methods can be specified using the *@UmlMethodGroup* annotation. Method groups will
be given labeled separators in class diagrams, making the list of methods easier to understand.

For example:

    @UmlMethodGroup("configuration")

could be used to label all the configuration-related methods in a type. Multiple *@UmlMethodGroup*
annotations can be added to specify that a method should be shown in more than one group.

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Automatic Method Groups <a name = "automatic-method-groups"></a>&nbsp;<img src="https://www.kivakit.org/images/gears-32.png" srcset="https://www.kivakit.org/images/gears-32-2x.png 2x"/>

The *@UmlMethodGroup* annotation can be avoided if the switch *-automatic-method-groups* is set to *true*
(which is the default). In this case, the best guess will be made based on method name and parameter
patterns as to which group a method most likely belongs. When the guess is inaccurate, or no guess is made,
an *@UmlMethodGroup* annotation can be applied to correct the result. The set of patterns that are used to
determine automatic groups is in a file called *lexakai.groups* in the documentation folder. This file can be
customized for a particular project.

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### UML Associations <a name = "uml-associations"></a>&nbsp; <img src="https://www.kivakit.org/images/right-arrow-32.png" srcset="https://www.kivakit.org/images/right-arrow-32-2x.png 2x"/>

Patterns in the names of types, fields and methods are used to try to deduce UML associations.
The annotation *@UmlRelation* can be used to override this guess with an explicit relation if the association
was not be deduced, or it was deduced incorrectly.

If the annotation *@UmlRelation(label = \"*label*\")* is applied to a method or field, a relation arrow will
be drawn from the enclosing type to the type of the member. Similar annotations are available for field
aggregation (*@UmlAggregation*) and composition (*@UmlComposition*). If the *@UmlRelation* tag is applied to a
type, it can specify a relation with a particular referent and cardinality. An explicit relation of this type
can be made specific to a particular diagram with the *diagram* parameter. Multiple *@UmlRelation* tags can
be applied to a type.

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Excluding Types And Members <a name = "excluding-types-and-members"></a>&nbsp;<img src="https://www.kivakit.org/images/no-32.png" srcset="https://www.kivakit.org/images/no-32-2x.png 2x"/>

Methods and fields can be excluded entirely by labeling them with *@UmlExcludeMember*.

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Non-Public Apis <a name = "non-public-apis"></a>

*@UmlNotPublicApi* marks a type or member as private even if it is not actually private.

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Annotation Summary <a name = "annotation-summary"></a>&nbsp; <img src="https://www.kivakit.org/images/annotation-32.png" srcset="https://www.kivakit.org/images/annotation-32-2x.png 2x"/>

    Diagrams:

           @UmlClassDiagram - declares the diagram(s) that the annotated type should be included in
            @UmlMethodGroup - includes the annotated method in a labeled method group in the diagram
                   @UmlNote - adds a callout note to a type or method

    Visibility:

      @UmlExcludeSuperTypes - excludes the listed supertypes from all diagrams
          @UmlExcludeMember - excludes the annotated member
          @UmlIncludeMember - includes the annotated member, even if it wouldn't normally be included
           @UmlNotPublicApi - marks the annotated type or member as private even if it is not

    Associations:

               @UmlRelation - adds a labeled UML relation from the enclosing type to the annotated member type
            @UmlAggregation - adds a UML aggregation association from the enclosing type to the annotated field type
            @UmlComposition - adds a UML composition association from the enclosing type to the annotated field type

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Configuration <a name = "configuration"></a>&nbsp; <img src="https://www.kivakit.org/images/tools-32.png" srcset="https://www.kivakit.org/images/tools-32-2x.png 2x"/>

The *documentation* folder for each project should contain a *lexakai* folder with a *lexakai.properties* file that looks similar to this:

    #
    # Project
    #
    project-name         = my-project
    project-description  = This is my project.
    project-javadoc-url  = https://me.github.io/myproject/javadoc/myproject
    project-icon         = https://www.kivakit.org/images/myproject.png
    project-footer       = Copyright by Me

    #
    # Diagram Titles
    #
    diagram-my-project   = My Project

The diagram name (the lowercase, hyphenated version of the marker interface) is used as a key to locate the 
title of the diagram. For example:

    @UmlClassDiagram(diagram = DiagramMyProject.class)

refers to the diagram title specified by the key *diagram-my-project* in the *lexakai.properties* file.

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Targeting Output to a Different Repository <a name = "targeting-output-to-a-different-repository"></a>

Lexakai is designed to direct output to a different repository. For example, in the [KivaKit](https://www.kivakit.org) 
project, output can be directed to a different folder with:

    -output-folder=[root-output-folder]

This allows KivaKit to update README.md diagrams in the source tree while directing diagrams and images produced 
by Lexakai to [kivakit-data](https://github.com/Telenav/kivakit-data). 

Storing documentation (Lexakai and Javadoc) and binary data like applications in a separate repository removes 
distracting files from the source repository and makes it faster to check out.

By default, Lexakai targets the source tree, but it is recommended to supply an output folder in another repository.

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Readme Generation and Updating <a name = "readme-generation-and-updating"></a>&nbsp; <img src="https://www.kivakit.org/images/pencil-32.png" srcset="https://www.kivakit.org/images/pencil-32-2x.png 2x"/>

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

<img src="https://www.lexakai.org/images/short-horizontal-line-128.png" srcset="https://www.lexakai.org/images/horizontal-line-128-2x.png 2x"/>

### Custom README Templates <a name = "custom-readme-templates"></a>

The first run of Lexakai on a project will create two default templates in the *documentation* folder
one for projects with source code and one for parent projects (projects with sub-projects). These
template files can be modified to produce custom output. To revert to the default templates, simply
remove them and run Lexakai again.

[//]: # (end-user-text)

<img src="https://www.kivakit.org/images/short-horizontal-line-128.png" srcset="https://www.kivakit.org/images/short-horizontal-line-128@2x.png 2x"/>

### Class Diagrams <a name="class-diagrams"></a> &nbsp; &nbsp; <img src="https://www.lexakai.org/images/diagram-32.png" srcset="https://www.lexakai.org/images/diagram-32-2x.png 2x"/>

None

<img src="https://www.kivakit.org/images/short-horizontal-line-128.png" srcset="https://www.kivakit.org/images/short-horizontal-line-128@2x.png 2x"/>

### Package Diagrams <a name="package-diagrams"></a> &nbsp;&nbsp; <img src="https://www.lexakai.org/images/box-32.png" srcset="https://www.lexakai.org/images/box-32-2x.png 2x"/>

[*com.telenav.lexakai*](https://www.lexakai.org/lexakai/lexakai/documentation/diagrams/com.telenav.lexakai.svg)  
[*com.telenav.lexakai.associations*](https://www.lexakai.org/lexakai/lexakai/documentation/diagrams/com.telenav.lexakai.associations.svg)  
[*com.telenav.lexakai.builders*](https://www.lexakai.org/lexakai/lexakai/documentation/diagrams/com.telenav.lexakai.builders.svg)  
[*com.telenav.lexakai.builders.grouper*](https://www.lexakai.org/lexakai/lexakai/documentation/diagrams/com.telenav.lexakai.builders.grouper.svg)  
[*com.telenav.lexakai.dependencies*](https://www.lexakai.org/lexakai/lexakai/documentation/diagrams/com.telenav.lexakai.dependencies.svg)  
[*com.telenav.lexakai.indexes*](https://www.lexakai.org/lexakai/lexakai/documentation/diagrams/com.telenav.lexakai.indexes.svg)  
[*com.telenav.lexakai.javadoc*](https://www.lexakai.org/lexakai/lexakai/documentation/diagrams/com.telenav.lexakai.javadoc.svg)  
[*com.telenav.lexakai.library*](https://www.lexakai.org/lexakai/lexakai/documentation/diagrams/com.telenav.lexakai.library.svg)  
[*com.telenav.lexakai.members*](https://www.lexakai.org/lexakai/lexakai/documentation/diagrams/com.telenav.lexakai.members.svg)  
[*com.telenav.lexakai.types*](https://www.lexakai.org/lexakai/lexakai/documentation/diagrams/com.telenav.lexakai.types.svg)

<img src="https://www.kivakit.org/images/short-horizontal-line-128.png" srcset="https://www.kivakit.org/images/short-horizontal-line-128@2x.png 2x"/>

### Javadoc <a name="javadoc"></a> &nbsp;&nbsp; <img src="https://www.lexakai.org/images/books-32.png" srcset="https://www.lexakai.org/images/books-32-2x.png 2x"/>

Javadoc coverage for this project is 48.8%.  
  
&nbsp; &nbsp;  ![](https://www.lexakai.org/images/meter-50-12.png)

The following significant classes are undocumented:  

- AssociationBuilder  
- Associations  
- JavadocCoverage  
- LexakaiClassDiagram  
- LexakaiProjectProperties  
- MavenDependencyTreeBuilder  
- MethodGroup  
- MethodGroupNameGuesser  
- Types  
- UmlAssociation  
- UmlMethod

| Class | Documentation Sections |
|---|---|
| [*Annotations*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/library/Annotations.html) |  |  
| [*Artifact*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/dependencies/Artifact.html) |  |  
| [*AssociationBuilder*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/builders/AssociationBuilder.html) |  |  
| [*Associations*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/library/Associations.html) |  |  
| [*BaseLexakaiDiagram*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/BaseLexakaiDiagram.html) |  |  
| [*Dependency*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/dependencies/Dependency.html) |  |  
| [*DependencyDiagram*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/dependencies/DependencyDiagram.html) |  |  
| [*DependencyTree*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/dependencies/DependencyTree.html) |  |  
| [*Diagrams*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/library/Diagrams.html) |  |  
| [*Fields*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/library/Fields.html) |  |  
| [*JavadocCoverage*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/javadoc/JavadocCoverage.html) |  |  
| [*Lexakai*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/Lexakai.html) |  |  
| [*LexakaiClassDiagram*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/LexakaiClassDiagram.html) |  |  
| [*LexakaiClassDiagram.Referent*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/LexakaiClassDiagram.Referent.html) |  |  
| [*LexakaiProject*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/LexakaiProject.html) | Java Parsing |  
| | Functions |  
| | Settings |  
| [*LexakaiProjectFiles*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/LexakaiProjectFiles.html) |  |  
| [*LexakaiProjectFolders*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/LexakaiProjectFolders.html) |  |  
| [*LexakaiProjectProperties*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/LexakaiProjectProperties.html) |  |  
| [*MavenDependencyTreeBuilder*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/dependencies/MavenDependencyTreeBuilder.html) |  |  
| [*Members*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/library/Members.html) |  |  
| [*MethodBuilder*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/builders/MethodBuilder.html) |  |  
| [*MethodGroup*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/builders/grouper/MethodGroup.html) |  |  
| [*MethodGroupNameGuesser*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/builders/grouper/MethodGroupNameGuesser.html) |  |  
| [*MethodGrouper*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/builders/grouper/MethodGrouper.html) |  |  
| [*MethodGroups*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/builders/grouper/MethodGroups.html) |  |  
| [*Methods*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/library/Methods.html) |  |  
| [*Names*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/library/Names.html) |  |  
| [*Names.Qualification*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/library/Names.Qualification.html) |  |  
| [*Names.TypeParameters*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/library/Names.TypeParameters.html) |  |  
| [*ReadMeUpdater*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/indexes/ReadMeUpdater.html) | Usage |  
| | Templates |  
| [*TypeBuilder*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/builders/TypeBuilder.html) |  |  
| [*Types*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/library/Types.html) |  |  
| [*UmlAssociation*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/associations/UmlAssociation.html) |  |  
| [*UmlAssociation.AssociationType*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/associations/UmlAssociation.AssociationType.html) |  |  
| [*UmlConstructor*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/members/UmlConstructor.html) |  |  
| [*UmlInheritance*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/associations/UmlInheritance.html) |  |  
| [*UmlMethod*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/members/UmlMethod.html) |  |  
| [*UmlType*](https://www.lexakai.org/javadoc/lexakai/lexakai/com/telenav/lexakai/types/UmlType.html) |  |  

[//]: # (start-user-text)



[//]: # (end-user-text)

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512@2x.png 2x"/>

<sub>Copyright &#169; 2011-2021 [Telenav](http://telenav.com), Inc. Distributed under [Apache License, Version 2.0](LICENSE)</sub>  
<sub>This documentation was generated by [Lexakai](https://github.com/Telenav/lexakai). UML diagrams courtesy
of [PlantUML](http://plantuml.com).</sub>

