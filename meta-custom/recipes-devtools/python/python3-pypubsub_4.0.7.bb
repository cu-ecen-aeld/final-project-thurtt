SUMMARY = "Python Publish-Subscribe Mechanism"
HOMEPAGE = "https://github.com/schollii/pypubsub"
LICENSE = "BSD-2-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2fedfd31700f60e5c8d6499d70311882"

SRC_URI = "git://github.com/schollii/pypubsub.git;protocol=https;nobranch=1"
SRCREV = "v4.0.7"
SRC_URI[sha256sum] = "ec8b5cb147624958320e992602380cc5d0e4b36b1c59844d05e425a3003c09dc"

inherit pypi python_setuptools_build_meta

S = "${WORKDIR}/git"

DEPENDS += "python3-setuptools-scm-native"

RDEPENDS:${PN} += " \
    python3-core \
"

BBCLASSEXTEND = "native nativesdk"
