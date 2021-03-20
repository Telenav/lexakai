## Lexakai - Annotations &nbsp; ![](../documentation/images/tag-40.png)

Lexakai annotations. For usage details, see [Lexakai](../lexakai/README.md).

### Dependencies &nbsp; ![](../documentation/images/dependencies-40.png)

    <dependency>
        <groupId>com.telenav.tdk.core</groupId>
        <artifactId>com.telenav.tdk.core-application</artifactId>
        <version>8.1.1-SNAPSHOT</version>
    </dependency>

### Annotations &nbsp; ![](../documentation/images/annotation-32.png)

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
