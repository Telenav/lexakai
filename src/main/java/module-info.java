open module lexakai
{
    requires transitive lexakai.annotations;

    requires transitive kivakit.application;
    requires transitive kivakit.resource;
    requires transitive kivakit.network.http;

    requires transitive com.github.javaparser.core;
    requires transitive com.github.javaparser.symbolsolver.core;

    exports com.telenav.lexakai;
    exports com.telenav.lexakai.javadoc;
    exports com.telenav.lexakai.associations;
    exports com.telenav.lexakai.library;
    exports com.telenav.lexakai.dependencies;
    exports com.telenav.lexakai.builders;
    exports com.telenav.lexakai.builders.grouper;
    exports com.telenav.lexakai.indexes;
    exports com.telenav.lexakai.members;
    exports com.telenav.lexakai.types;
}
