SUMMARY = "Yder logging library"
HOMEPAGE = "https://github.com/babelouest/yder"
LICENSE = "LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=40d2542b8c43a3ec2b7f5da31a697b88"

SRC_URI = "git://github.com/babelouest/yder.git;protocol=https;branch=master;tag=v1.4.20"
S = "${WORKDIR}/git"

DEPENDS = "orcania"

inherit cmake

EXTRA_OECMAKE = "-DBUILD_TESTING=OFF -DBUILD_YDER_DOCUMENTATION=OFF -DBUILD_YDER_TESTING=OFF -DWITH_JOURNALD=OFF"
