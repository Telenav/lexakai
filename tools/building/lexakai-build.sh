#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

source library-functions.sh
source library-build.sh
source lexakai-projects.sh

for project_home in "${LEXAKAI_PROJECT_HOMES[@]}"; do

    build $project_home $@

done