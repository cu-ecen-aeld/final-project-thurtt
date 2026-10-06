SUMMARY = "Ulfius HTTP framework"
HOMEPAGE = "https://github.com/babelouest/ulfius"
LICENSE = "LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=40d2542b8c43a3ec2b7f5da31a697b88"

SRC_URI = "git://github.com/babelouest/ulfius.git;protocol=https;branch=master;tag=v2.7.15"
S = "${WORKDIR}/git"

DEPENDS = "orcania yder libmicrohttpd gnutls jansson zlib"

inherit cmake pkgconfig

EXTRA_OECMAKE = "-DBUILD_TESTING=OFF -DBUILD_ULFIUS_DOCUMENTATION=OFF -DWITH_GNUTLS=ON -DWITH_JANSSON=ON -DWITH_CURL=OFF"
