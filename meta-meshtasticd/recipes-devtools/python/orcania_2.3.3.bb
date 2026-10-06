SUMMARY = "Orcania, generic C library"
HOMEPAGE = "https://github.com/babelouest/orcania"
LICENSE = "LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=fc178bcd425090939a8b634d1d6a9594"

SRC_URI = "git://github.com/babelouest/orcania.git;protocol=https;branch=master;tag=v2.3.3"
S = "${WORKDIR}/git"

DEPENDS = "zlib"

inherit cmake

EXTRA_OECMAKE = "-DBUILD_TESTING=OFF -DBUILD_ORCANIA_DOCUMENTATION=OFF -DBUILD_ORCANIA_TESTING=OFF"
