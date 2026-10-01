SUMMARY = "A simple Python library to format datetime into timeago string"
HOMEPAGE = "https://github.com/hustcc/timeago"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d7dd917fab8f86c826b43d7ee2b9e481"

SRC_URI = "git://github.com/hustcc/timeago.git;protocol=https;branch=master"
SRCREV = "3614536007c4e1dde6a04fc2485503e7e46d23e7"

S = "${WORKDIR}/git"

inherit setuptools3

RDEPENDS:${PN} += " \
    python3-core \
    python3-datetime \
"

BBCLASSEXTEND = "native nativesdk"
