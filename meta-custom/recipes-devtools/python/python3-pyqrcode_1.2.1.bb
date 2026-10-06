SUMMARY = "A QR code generator module written pure Python"
HOMEPAGE = "https://github.com/mxrch/pyqrcode"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://PKG-INFO;beginline=8;endline=8;md5=e910b35b0ef4e1f665b9a75d6afb7709"

SRC_URI[sha256sum] = "fdbf7634733e56b72e27f9bce46e4550b75a3a2c420414035cae9d9d26b234d5"

PYPI_PACKAGE = "PyQRCode"

inherit pypi setuptools3

RDEPENDS:${PN} += " \
    python3-core \
"

BBCLASSEXTEND = "native nativesdk"
