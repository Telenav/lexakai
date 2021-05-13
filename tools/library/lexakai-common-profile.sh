#―――― Lexakai ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――

if [ -z "$LEXAKAI_WORKSPACE" ]; then

    system_variable LEXAKAI_WORKSPACE "$KIVAKIT_WORKSPACE"

fi

system_variable LEXAKAI_HOME "$LEXAKAI_WORKSPACE/lexakai"
system_variable LEXAKAI_ANNOTATIONS_HOME "$LEXAKAI_WORKSPACE/lexakai-annotations"
system_variable LEXAKAI_ASSETS_HOME "$LEXAKAI_WORKSPACE/lexakai-assets"
system_variable LEXAKAI_VERSION "$(project_version $LEXAKAI_HOME)"
system_variable LEXAKAI_BUILD "$(project_build $LEXAKAI_HOME)"
system_variable LEXAKAI_TOOLS "$LEXAKAI_HOME/tools"

append_path "$LEXAKAI_TOOLS/building"
append_path "$LEXAKAI_TOOLS/developing"
append_path "$LEXAKAI_TOOLS/library"
append_path "$LEXAKAI_TOOLS/releasing"

source $LEXAKAI_TOOLS/library/lexakai-projects.sh

echo " "
echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫ Lexakai Environment ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
echo "┋"
echo -e "┋          LEXAKAI_WORKSPACE: ${ATTENTION}$LEXAKAI_WORKSPACE${NORMAL}"
echo -e "┋               LEXAKAI_HOME: ${ATTENTION}$LEXAKAI_HOME${NORMAL}"
echo -e "┋            LEXAKAI_VERSION: ${ATTENTION}$LEXAKAI_VERSION${NORMAL}"
echo -e "┋              LEXAKAI_BUILD: ${ATTENTION}$LEXAKAI_BUILD${NORMAL}"
echo "┋"
echo "┋        LEXAKAI_ASSETS_HOME: $LEXAKAI_ASSETS_HOME"
echo "┋   LEXAKAI_ANNOTATIONS_HOME: $LEXAKAI_ANNOTATIONS_HOME"
echo "┋"
echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
echo " "

cd $LEXAKAI_HOME
