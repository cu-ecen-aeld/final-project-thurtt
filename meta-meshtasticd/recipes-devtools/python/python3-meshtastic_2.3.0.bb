# Recipe created by recipetool
# This is the basis of a recipe and may need further editing in order to be fully functional.
# (Feel free to remove these comments when editing.)

SUMMARY = "Python API & client shell for talking to Meshtastic devices"
HOMEPAGE = "https://github.com/meshtastic/python"
# NOTE: License in setup.py/PKGINFO is: GPL-3.0-only
# WARNING: the following LICENSE and LIC_FILES_CHKSUM values are best guesses - it is
# your responsibility to verify that the values are complete and correct.
# NOTE: Original package / source metadata indicates license is: GPL-3.0-only
#
# NOTE: multiple licenses have been detected; they have been separated with &
# in the LICENSE value for now since it is a reasonable assumption that all
# of the licenses apply. If instead there is a choice between the multiple
# licenses then you should change the value to separate the licenses with |
# instead of &. If there is any doubt, check the accompanying documentation
# to determine which situation is applicable.
LICENSE = "Apache-2.0 & GPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRC_URI[sha256sum] = "d375f3108cf48a5911b8e423bfc9a28247561b8bc10af5e88369413ba9566092"

inherit pypi setuptools3

# The following configs & dependencies are from setuptools extras_require.
# These dependencies are optional, hence can be controlled via PACKAGECONFIG.
# The upstream names may not correspond exactly to bitbake package names.
# The configs are might not correct, since PACKAGECONFIG does not support expressions as may used in requires.txt - they are just replaced by text.
#
# Uncomment this line to enable all the optional features.
#PACKAGECONFIG ?= "tunnel"
PACKAGECONFIG[tunnel] = ",,,python3-pytap2"

# WARNING: the following rdepends are from setuptools install_requires. These
# upstream names may not correspond exactly to bitbake package names.
RDEPENDS:${PN} += "python3-bleak python3-dotmap python3-pexpect python3-protobuf python3-pypubsub python3-pyqrcode python3-pyserial python3-pyyaml python3-requests python3-tabulate python3-timeago"

# WARNING: the following rdepends are determined through basic analysis of the
# python sources, and might not be 100% accurate.
RDEPENDS:${PN} += "python3-asyncio python3-core python3-datetime python3-io python3-json python3-logging python3-math python3-netclient python3-pkg-resources python3-threading"

# WARNING: We were unable to map the following python package/module
# dependencies to the bitbake packages which include them:
#    google.protobuf
#    google.protobuf.internal
#    google.protobuf.json_format
#    meshtastic.__init__
#    pubsub
#    serial
#    serial.tools.list_ports
#    yaml

PYPI_PACKAGE = "meshtastic"
