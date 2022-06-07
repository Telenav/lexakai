open module lexakai
{
    // Cactus Build
    requires cactus.build.metadata;
    requires cactus.build.maven.model;

    // KivaKit
    requires kivakit.application;

    // Java Parsing
    requires com.github.javaparser.core;
    requires com.github.javaparser.symbolsolver.core;

    exports com.telenav.lexakai;
}
