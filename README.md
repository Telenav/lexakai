[//]: # (start-user-text)

<a href="https://www.lexakai.org">
<img src="https://www.lexakai.org/images/web-32.png" srcset="https://www.lexakai.org/images/web-32-2x.png 2x"/>
</a>
&nbsp;
<a href="https://twitter.com/openlexakai">
<img src="https://www.lexakai.org/images/twitter-32.png" srcset="https://www.lexakai.org/images/twitter-32-2x.png 2x"/>
</a>
&nbsp;
<a href="https://lexakai.zulipchat.com">
<img src="https://www.lexakai.org/images/zulip-32.png" srcset="https://www.lexakai.org/images/zulip-32-2x.png 2x"/>
</a>

<p></p>

<img src="https://www.lexakai.org/images/lexakai-background-1024.png" srcset="https://www.lexakai.org/images/lexakai-background-1024-2x.png 2x"/>

[//]: # (end-user-text)

# Lexakai &nbsp;&nbsp; <img src="https://www.lexakai.org/images/lexakai-64.png" srcset="https://www.lexakai.org/images/lexakai-64-2x.png 2x"/>

A tool for generating UML diagrams and markdown documentation indexes.

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"/>

### Index

[**Summary**](#summary)  
[**Project Resources**](#project-resources)  
[**Setup**](#setup)  
[**Examples**](#examples)  
[**Creating Basic UML Package Diagrams**](#creating-basic-uml-package-diagrams)  
[**Custom UML Diagrams**](#custom-uml-diagrams)  
[**Grouping Methods in Class Diagrams**](#grouping-methods-in-class-diagrams)  
[**Automatic Method Groups**](#automatic-method-groups)  
[**UML Associations**](#uml-associations)  
[**Excluding Types And Members**](#excluding-types-and-members)  
[**Non-Public Apis**](#non-public-apis)  
[**Annotation Summary**](#annotation-summary)  
[**Settings**](#settings)  
[**Readme Generation and Updating**](#readme-generation-and-updating)  
[**Preserving Text Between Updates**](#preserving-text-between-updates)  
[**Custom README Templates**](#custom-readme-templates)  

[**Dependencies**](#dependencies) | [**Class Diagrams**](#class-diagrams) | [**Package Diagrams**](#package-diagrams) | [**Javadoc**](#javadoc)

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"/>

### Dependencies <a name="dependencies"></a> &nbsp;&nbsp; <img src="https://www.kivakit.org/images/dependencies-32.png" srcset="https://www.kivakit.org/images/dependencies-32-2x.png 2x"/>

[*Dependency Diagram*](https://www.lexakai.org/lexakai/lexakai/documentation/diagrams/dependencies.svg)

#### Maven Dependency

    <dependency>
        <groupId>com.telenav.lexakai</groupId>
        <artifactId>lexakai</artifactId>
        <version>0.9.5-alpha-SNAPSHOT</version>
    </dependency>


<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

[//]: # (start-user-text)

### Summary <a name = "summary"></a>

*The palest ink is better than the best memory.*

&nbsp; &nbsp; &nbsp; &mdash; Chinese proverb

*Lexakai* - from lexis (greek for *word*) and kai (hawaiian for *ocean*).

Lexakai creates documentation indexes and UML diagrams from the source code of each maven or gradle project
discovered recursively from the root folder(s) given as argument(s). 

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>


### Project Resources <a name = "project-resources"></a> &nbsp; <img src="https://www.kivakit.org/images/water-32.png" srcset="https://www.kivakit.org/images/water-32-2x.png 2x"/>

| Resource     |     Description                   |
|--------------|-----------------------------------|
| Project Name | Lexakai |
| Summary | Lexakai creates markdown and UML from Java source code |
| Javadoc Coverage |  <!-- <img src="https://www.kivakit.org/images/meter-50-96.png" srcset="https://www.kivakit.org/images/meter-50-96-2x.png 2x"/> --> <img src="https://www.kivakit.org/images/meter-50-96.png" srcset="https://www.kivakit.org/images/meter-50-96-2x.png 2x"/><!-- end --> |
| Lead | Jonathan Locke (Luo, Shibo) <br/> [jonathanl@telenav.com](mailto:jonathanl@telenav.com) |
| Administrator | Jonathan Locke (Luo, Shibo) <br/> [jonathanl@telenav.com](mailto:jonathanl@telenav.com) |
| Email | [kivakit@telenav.com](mailto:jonathanl@telenav.com) |
| Chat | [Zulip](https://lexakai.zulip.com) |
| Twitter | [@OpenLexakai](https://twitter.com/openlexakai) |
| Issues | [GitHub Issues](https://github.com/Telenav/lexakai/issues) |
| Code | [GitHub](https://github.com/Telenav/lexakai) |
| Checkout | `git clone https://github.com/Telenav/lexakai.git` |

<p>
<br/>
</p>

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Setup <a name = "setup"></a> &nbsp; <a name = "project-resources"></a> &nbsp; <img src="https://www.kivakit.org/images/box-32.png" srcset="https://www.kivakit.org/images/box-32-2x.png 2x"/>

1. Install [GraphViz](https://plantuml.com/graphviz-dot) for generating SVG files

2. Download Lexakai

   [**Lexakai 0.9.5-alpha-SNAPSHOT**](https://www.lexakai.org/builds/lexakai-0.9.5-alpha-SNAPSHOT.jar) &nbsp; ![](http://www.kivakit.org/images/down-arrow-32.png)  


3. Run Lexakai with *java -jar*

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Examples <a name = "example"></a>

Examples of Lexakai documentation:

- [KivaKit](https://github.com/Telenav/kivakit)
- [Lexakai](https://github.com/Telenav/lexakai)

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Creating Basic UML Package Diagrams <a name = "creating-basic-uml-package-diagrams"></a>&nbsp; ![](http://www.kivakit.org/images/box-32.png)

Lexakai automatically creates basic package diagrams for all public types without further configuration.

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Custom UML Diagrams <a name = "custom-uml-diagrams"></a>&nbsp; &nbsp; ![](http://www.kivakit.org/images/diagram-40.png)

To create a set of one or more custom diagrams for a project, annotations can be used from this project:

    <dependency>
        <groupId>com.telenav.lexakai</groupId>
        <artifactId>lexakai-annotations</artifactId>
        <version>0.9.3</version>
    </dependency>

A class, annotation, enum or interface annotated with *@UmlClassDiagram(diagram = [marker-interface].class)*, where *[marker-interface]*
extends the interface *UmlDiagramIdentifier*, will be included in the specified diagram.

For example, a type annotated with:

    @UmlClassDiagram(diagram = DiagramMapServer.class)

will be included in the diagram *diagram-map-server* (the lowercase, hyphenated name derived from the marker interface name). A type can be
used in more than one diagram, by specifying more than one
*@UmlClassDiagram* annotation:

    @UmlClassDiagram(diagram = DiagramMapServer.class)
    @UmlClassDiagram(diagram = DiagramServers.class)

The preferred location for diagram marker interfaces is in the sub-package *project.lexakai.diagrams*, and they should be prefixed with *
Diagram* to make their use clear.

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Grouping Methods in Class Diagrams <a name = "grouping-methods-in-class-diagrams"></a>&nbsp; ![](http://www.kivakit.org/images/set-32.png)

Groups of methods can be specified using the *@UmlMethodGroup* annotation. Method groups will be given labeled separators in class diagrams,
making the list of methods easier to understand.

For example:

    @UmlMethodGroup("settings")

could be used to label all the settings-related methods in a type. Multiple *@UmlMethodGroup*
annotations can be added to specify that a method should be shown in more than one group.

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Automatic Method Groups <a name = "automatic-method-groups"></a>&nbsp;![](http://www.kivakit.org/images/gears-32.png)

The *@UmlMethodGroup* annotation can be avoided if the switch *-automatic-method-groups* is set to *true*
(which is the default). In this case, the best guess will be made based on method name and parameter patterns 
as to which group a method most likely belongs. When the guess is inaccurate, or no guess is made, 
an *@UmlMethodGroup* annotation can be applied to correct the result. The set of patterns that are used to 
determine automatic groups is in a file called *lexakai.groups* in the Lexakai settings folder.
This file can be customized for a particular project. An explicit annotation can be used to override 
any automatic group assignment(s) for a method.

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### UML Associations <a name = "uml-associations"></a>&nbsp; ![](http://www.kivakit.org/images/right-arrow-32.png)

Patterns in the names of types, fields and methods are used to try to deduce UML associations. The annotation *@UmlRelation* can be used to
override this guess with an explicit relation if the association was not be deduced, or it was deduced incorrectly.

If the annotation *@UmlRelation(label = \"*label*\")* is applied to a method or field, a relation arrow will be drawn from the enclosing
type to the type of the member. Similar annotations are available for field aggregation (*@UmlAggregation*) and composition (*
@UmlComposition*). If the *@UmlRelation* tag is applied to a type, it can specify a relation with a particular referent and cardinality. An
explicit relation of this type can be made specific to a particular diagram with the *diagram* parameter. Multiple *@UmlRelation* tags can
be applied to a type.

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Excluding Types And Members <a name = "excluding-types-and-members"></a>&nbsp;![](http://www.kivakit.org/images/no-32.png)

Methods and fields can be excluded entirely by labeling them with *@UmlExcludeMember*.

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Non-Public Apis <a name = "non-public-apis"></a>

*@UmlNotPublicApi* marks a type or member as private even if it is not actually private.

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Annotation Summary <a name = "annotation-summary"></a>&nbsp; ![](http://www.kivakit.org/images/annotation-32.png)

#### Diagrams

- *@UmlClassDiagram* - declares the diagram(s) that the annotated type should be included in
- *@UmlMethodGroup* - includes the annotated method in a labeled method group in the diagram
- *@UmlNote* - adds a callout note to a type or method

#### Visibility

- *@UmlExcludeSuperTypes* - excludes the listed supertypes from all diagrams
- *@UmlExcludeMember* - excludes the annotated member
- *@UmlIncludeMember* - includes the annotated member, even if it wouldn't normally be included
- *@UmlNotPublicApi* - marks the annotated type or member as private even if it is not

#### Associations

- *@UmlRelation* - adds a labeled UML relation from the enclosing type to the annotated member type
- *@UmlAggregation* - adds a UML aggregation association from the enclosing type to the annotated field type
- *@UmlComposition* - adds a UML composition association from the enclosing type to the annotated field type

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Settings <a name = "settings"></a>&nbsp; ![](http://www.kivakit.org/images/tools-32.png)

Each root project that Lexakai processes must have a *documentation/lexakai* folder. This folder must contain 
all the settings that Lexakai uses to create documentation. When Lexakai is run for the first time, this folder will 
be created and populated with default settings files. Settings for individual projects can then be added to
the *projects* subfolder, so the final tree looks like this:

    + documentation
    \---+ lexakai
        |     lexakai.settings
        |     lexakai.theme
        |     lexakai.groups
        |     lexakai-source-readme-template.md
        |     lexakai-project-readme-template.md
        \---+ projects
                  my-project.properties
                  my-project-sub-project.properties

where these files are described as follows:

| Settings File     |     Description                                          |
|-------------------|----------------------------------------------------------|
| *lexakai.settings*  | Global settings for all projects under the root folder   |
| *lexakai.theme*     | PlantUML theme file copied to each diagram output folder |
| *lexakai.groups*    | Patterns used to automatically group methods             |
| *lexakai-source-readme-template.md*  | *README.md* template for projects that have source code |
| *lexakai-project-readme-template.md* | *README.md* template for projects that have child projects |
| *projects/[project].properties* | Markdown and UML settings for each project in the source tree |

The *.theme*, *.groups* and *README.md* template files can be customized. To perform a 'factory-reset' 
on these resources, run Lexakai with *-overwrite-resources=true* or simply remove the files and 
Lexakai will re-create them. The [project] value in the table above should be the hyphenated 
artifact id for the project, as defined in pom.xml, such as *kivakit-core-kernel*.

#### lexakai.settings

This global settings file contains the following properties:

    #
    # Locations of resources linked to from README.md files
    #

    lexakai-documentation-location = https://www.lexakai.org/lexakai
    lexakai-javadoc-location       = https://www.lexakai.org/javadoc
    lexakai-images-location        = https://www.lexakai.org/images

    project-footer                 = <sub>Copyright &#169; 2011-2021, Me</sub>

These values specify the location of resources for Lexakai when it is producing links in *README.md* files.
When using GitHub Pages, the folders *lexakai*, *javadoc* and *images* are normally in the *docs* folder 
in a documentation project and GitHub Pages is configured to share that folder with the world.

#### [project].properties

Each project in the source tree requires a *[project].properties* file in the *projects* folder, which looks like:

    #
    # Project
    #

    project-title            = kivakit-core-network-core
    project-description      = This module provides core networking functionality.
    project-icon             = nucleus-32
    
    #
    # Diagrams
    #

    diagram-port             = Hosts, Ports and Protocols
    diagram-network-location = Network Locations
    
The *project-title*, *project-description* and *project-icon* values will be used to populate values in the 
*README.md* templates (described above). The *project-icon* value is used as the base name of *[project-icon].png* and
*[project-icon]-2x.png*, in order to support HiDPI displays. The *diagrams* section provides titles for 
individual UML diagrams. The lowercase, hyphenated name of the marker interface (as described above) is used 
as a key to locate the title of the diagram. For example:

    @UmlClassDiagram(diagram = DiagramMyUtilities.class)

refers to the diagram title specified by the key *diagram-my-utilities* in the *.properties* file for the
project.

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Readme Generation and Updating <a name = "readme-generation-and-updating"></a>&nbsp; ![](http://www.kivakit.org/images/pencil-32.png)

If the *-update-readme* switch is set to *true* (it is *false* by default to ensure it doesn't overwrite an existing 
file), then a *README.md* file will be generated or updated each time the UML diagrams are generated. This markdown 
file will use *project-title* as its title and insert the description *project-description* from the *[project].properties* 
file as the project description. 

An index of project diagrams is updated along with an index of the Javadoc (which should be available at 
*lexakai-javadoc-location*) for all types. Sections of documentation in the Javadoc can also 
be indexed based on the pattern specified by the switch *-javadoc-section-pattern*. By default, this 
pattern is:

    <p><b>section-title</b></p>

which is the style used in KivaKit, but any regular expression pattern can be substituted.

### Preserving Text Between Updates <a name = "preserving-text-between-updates"></a>

Any text between the markdown comments *start-user-text* and *end-user-text* will be preserved, allowing 
additional documentation to be maintained.

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Custom README Templates <a name = "custom-readme-templates"></a>

The first run of Lexakai on a project will create two default templates in the *documentation/lexakai* settings 
folder one for projects with source code and one for parent projects (projects with sub-projects). These template 
files can be modified to produce custom output. To revert to the default templates, simply remove them and run 
Lexakai again.

[//]: # (end-user-text)

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Class Diagrams <a name="class-diagrams"></a> &nbsp; &nbsp; <img src="https://www.kivakit.org/images/diagram-40.png" srcset="https://www.kivakit.org/images/diagram-40-2x.png 2x"/>

None

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Package Diagrams <a name="package-diagrams"></a> &nbsp;&nbsp; <img src="https://www.kivakit.org/images/box-32.png" srcset="https://www.kivakit.org/images/box-32-2x.png 2x"/>

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

<img src="https://www.kivakit.org/images/horizontal-line-128.png" srcset="https://www.kivakit.org/images/horizontal-line-128-2x.png 2x"/>

### Javadoc <a name="javadoc"></a> &nbsp;&nbsp; <img src="https://www.kivakit.org/images/books-32.png" srcset="https://www.kivakit.org/images/books-32-2x.png 2x"/>

Javadoc coverage for this project is 48.7%.  
  
&nbsp; &nbsp; <img src="https://www.lexakai.org/images/meter-50-96.png" srcset="https://www.lexakai.org/images/meter-50-96-2x.png 2x"/>


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

<img src="https://www.kivakit.org/images/horizontal-line-512.png" srcset="https://www.kivakit.org/images/horizontal-line-512-2x.png 2x"/>

<sub>Copyright &#169; 2011-2021 [Telenav](http://telenav.com), Inc. Distributed under [Apache License, Version 2.0](LICENSE)</sub>  
<sub>This documentation was generated by [Lexakai](https://github.com/Telenav/lexakai). UML diagrams courtesy
of [PlantUML](http://plantuml.com).</sub>

