#!/usr/bin/perl

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

use strict;
use warnings FATAL => 'all';

#
# Include build script from cactus-build
#

system("git clone --branch develop --quiet https://github.com/Telenav/cactus-build.git");

require "./cactus-build/.github/scripts/build-include.pl";

#
# Clone repositories and build
#

my ($build_type) = @ARGV;
my $github = "https://github.com/Telenav";

clone("$github/kivakit", "dependency");
clone("$github/kivakit-extensions", "dependency");
clone("$github/lexakai", "build");

build_kivakit("package");

system("mvn -DgroupId=com.telenav.kivakit -DartifactId=kivakit-grpc-merged -Dfile=./kivakit-extensions/kivakit-merged-jars/lib/kivakit-grpc-merged-1.1.0.jar -Dversion=1.1.0 -Dpackaging=jar install:install-file");
system("mvn -DgroupId=com.telenav.kivakit -DartifactId=kivakit-protostuff-merged -Dfile=./kivakit-extensions/kivakit-merged-jars/lib/kivakit-protostuff-merged-1.1.0.jar -Dversion=1.1.0 -Dpackaging=jar install:install-file");
system("mvn -DgroupId=com.telenav.kivakit -DartifactId=kivakit-prometheus-merged -Dfile=./kivakit-extensions/kivakit-merged-jars/lib/kivakit-prometheus-merged-1.1.0.jar -Dversion=1.1.0 -Dpackaging=jar install:install-file");

build_kivakit_extensions("package");
build_lexakai($build_type);

