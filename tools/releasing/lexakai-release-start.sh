#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

source library-functions.sh
source lexakai-projects.sh

ARGUMENT_HELP="[version]"

version=$1

require_variable version

for project_home in "${LEXAKAI_PROJECT_HOMES[@]}"; do

    git_flow_release_start $project_home $version

done
