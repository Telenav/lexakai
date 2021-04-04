open module lexakai.application
{
    requires transitive lexakai.annotations;
    requires transitive kivakit.core.application;
    requires transitive kivakit.core.resource;
    requires transitive kivakit.core.network.http;

    requires transitive com.github.javaparser.core;
    requires transitive com.github.javaparser.symbolsolver.core;

    exports com.telenav.lexakai.application;
}
