#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

if [ -z "$LEXAKAI_HOME" ]; then
    echo "You must set up your environment to use Lexakai."
    echo "See https://www.lexakai.org/ for details."
    exit 1
fi

cd $LEXAKAI_WORKSPACE
git clone git@github.com:Telenav/lexakai-data.git

cd $LEXAKAI_HOME
git checkout develop
lexakai-build.sh all clean
