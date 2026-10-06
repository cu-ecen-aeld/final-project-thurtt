

SUMMARY = "Mesh status display daemon"
DESCRIPTION = "An service the reports system status to an oled display"
HOMEPAGE = "https://github.com/thurtt/mesh_statusd"

LICENSE = "GPL-3.0-only & MIT & Unlicense"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1ebbd3e34237af26da5dc08a4e440464 \
                    file://Unity/LICENSE.txt;md5=2060603d080eecf88d27a023cff61de2 \
                    file://lg/PY_LGPIO/LICENSE;md5=61287f92700ec1bdf13bc86d8228cd13 \
                    file://lg/PY_RGPIO/LICENSE;md5=61287f92700ec1bdf13bc86d8228cd13 \
                    file://lg/UNLICENCE;md5=61287f92700ec1bdf13bc86d8228cd13"

SRC_URI = "gitsm://github.com/thurtt/mesh_statusd.git;protocol=https;branch=main"

# Modify these as desired
SRCREV = "6a31a7bc65e6222226d7d22ca226e7251811a830"

inherit systemd
SYSTEMD_SERVICE:${PN} = "mesh_statusd.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

EXTRA_OEMAKE = "CC='${CC}' AR='${AR}' STRIP='${STRIP}' CROSS_COMPILE= TARGET_HEADERS= WERROR="

# the lgpio library already strips debug symbols and that makes bitbake irrationally
# angry. This tells bitbake to not enforce the debug stripping rules.
INSANE_SKIP:${PN} += "already-stripped"

S = "${WORKDIR}/git"

do_configure () {
}

do_compile() {
    oe_runmake
}

do_install () {
  # Install the systemd unit file
  install -d ${D}${systemd_unitdir}/system
  install -D -m 0644 ${S}/mesh_statusd.service ${D}${systemd_unitdir}/system/mesh_statusd.service

  # Install the daemon and support python script
  install -D -m 0755 ${S}/mesh_statusd ${D}${bindir}/mesh_statusd
  install -D -m 0755 ${S}/meshtastic_info.py ${D}${bindir}/meshtastic_info.py

  # Install the LGPIO shared library
  install -D -m 0755 ${S}/lg/liblgpio.so.1 ${D}${libdir}/liblgpio.so.1
  ln -sf liblgpio.so.1 ${D}${libdir}/liblgpio.so
}
