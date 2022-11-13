open module lexakai.library
{
    // Cactus
    requires cactus.maven.model;

    // KivaKit
    requires kivakit.application;

    // Java Parsing
    requires com.github.javaparser.core;
    requires com.github.javaparser.symbolsolver.core;

    exports com.telenav.lexakai;
}
