#!/bin/bash

#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#
#  © 2011-2021 Telenav, Inc.
#  Licensed under Apache License, Version 2.0
#
#///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

source library-functions.sh
source lexakai-projects.sh

version="$1"

require_variable version "[version]"

snapshot_version="${1%-SNAPSHOT}-SNAPSHOT"

update_version $LEXAKAI_ANNOTATIONS_HOME $snapshot_version
update_version $LEXAKAI_HOME $snapshot_version
