#!/bin/bash
# Script to build image for qemu.
# Author: Siddhant Jajoo.

git submodule init
git submodule sync
git submodule update

# Directory this script is located
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# local.conf won't exist until this step on first execution
source poky/oe-init-build-env

# The layers that need to be added to the build
# LAYER_NAMES and LAYER_PATHS are associated. You need to change or update both
# arrays when adding new layers to the build
LAYER_NAMES=(
    "meta-raspberrypi"
    "meta-oe"
    "meta-python"
    "meta-networking"
    "meta-multimedia"
    "meta-meshtasticd"

)

LAYER_PATHS=(
    "$SCRIPT_DIR/meta-raspberrypi"
    "$SCRIPT_DIR/meta-openembedded/meta-oe"
    "$SCRIPT_DIR/meta-openembedded/meta-python"
    "$SCRIPT_DIR/meta-openembedded/meta-networking"
    "$SCRIPT_DIR/meta-openembedded/meta-multimedia"
    "$SCRIPT_DIR/meta-meshtasticd"

)

CONFLINE="MACHINE = \"raspberrypi4-64\""

# copy the baseline config file to the build directory
cp $SCRIPT_DIR/conf/local.conf conf/local.conf

cat conf/local.conf | grep "${CONFLINE}" > /dev/null
local_conf_info=$?

if [ $local_conf_info -ne 0 ];then
	echo "Append ${CONFLINE} in the local.conf file"
	echo ${CONFLINE} >> conf/local.conf

else
	echo "${CONFLINE} already exists in the local.conf file"
fi

# add any layers that haven't already been added
for i in "${!LAYER_NAMES[@]}"; do
    layer="${LAYER_NAMES[$i]}"
    layer_path="${LAYER_PATHS[$i]}"
    echo "Working on layer $layer, value: $layer_path"
    bitbake-layers show-layers | grep "$layer" > /dev/null
    layer_info=$?

    if [ $layer_info -ne 0 ];then
    	echo "Adding $i layer at path: $layer_path"
    	bitbake-layers add-layer $layer_path
    else
    	echo "$layer layer already exists"
    fi
    ((idx++))
done

set -e
bitbake meshtasticd-image
