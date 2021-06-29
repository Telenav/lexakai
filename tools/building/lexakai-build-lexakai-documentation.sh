#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

source library-functions.sh
source lexakai-projects.sh

for project_home in "${LEXAKAI_PROJECT_HOMES[@]}"; do

    project_name=$(project_name $project_home)

    echo "lexakai -project-version=$LEXAKAI_VERSION -output-folder=$LEXAKAI_ASSETS_HOME/docs/$LEXAKAI_VERSION/lexakai/$project_name $project_home"

    lexakai -project-version=$LEXAKAI_VERSION -output-folder=$LEXAKAI_ASSETS_HOME/docs/$LEXAKAI_VERSION/lexakai/$project_name $project_home

done
