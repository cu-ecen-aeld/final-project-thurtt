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
declare -A layers; declare -A layer_paths
layers+=("meta-raspberrypi"); layer_paths+=("$SCRIPT_DIR/meta-raspberrypi")
layers+=("meta-oe"); layer_paths+=("$SCRIPT_DIR/meta-openembedded/meta-oe")
layers+=("meta-python"); layer_paths+=("$SCRIPT_DIR/meta-openembedded/meta-python")
layers+=("meta-networking"); layer_paths+=("$SCRIPT_DIR/meta-openembedded/meta-networking")
layers+=("meta-multimedia"); layer_paths+=("$SCRIPT_DIR/meta-openembedded/meta-multimedia")
# layers["meta-raspberrypi"]=$SCRIPT_DIR/meta-raspberrypi
# layers["meta-oe"]=$SCRIPT_DIR/meta-openembedded/meta-oe
# layers["meta-python"]=$SCRIPT_DIR/meta-openembedded/meta-python
# layers["meta-networking"]=$SCRIPT_DIR/meta-openembedded/meta-networking
# layers["meta-multimedia"]=$SCRIPT_DIR/meta-openembedded/meta-multimedia

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
for i in "${!layers[@]}"
do
    bitbake-layers show-layers | grep "${layers[$i]}" > /dev/null
    layer_info=$?

    if [ $layer_info -ne 0 ];then
    	echo "Adding $i layer"
    	bitbake-layers add-layer ${layer_paths[$i]}
    else
    	echo "${layers[$i]} layer already exists"
    fi
done

set -e
bitbake core-image-base
