###############################################################################
#
# This recipe is adapted from https://github.com/buildroot-meshtastic/meta-meshtastic
# License coveys from the original repo and is included in this recipe
#
###############################################################################

inherit python3native

SUMMARY = "meshtasticd firmware daemon"
DESCRIPTION = "meshtasticd is the firmware daemon for Meshtastic"
HOMEPAGE = "https://github.com/meshtastic/firmware"
LICENSE = "GPL-3.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8f0e2cd40e05189ec81232da84bd6e1a"

SRC_URI = "gitsm://github.com/meshtastic/firmware;protocol=https;branch=2.7"
SRC_URI[sha256sum] = "3977fb33d30835f4fcde290a5ef88d00affca2a5dcf5415555adff3e3b39336a"
SRCREV = "54e0d8d0ab2ff56b3a9ce967e53f79e49af560fb"

# platformio insists on downloading packages during the compiliation process
do_compile[network] = "1"

# Where to find the source once fetched
S = "${WORKDIR}/git"

DEPENDS += "\
    pkgconf \
    zlib \
    openssl-native \
    python3-native \
    python3-platformio-native \
    libgpiod yaml-cpp bluez5 i2c-tools libusb1 libbsd libuv\
"

RDEPENDS:${PN} += " \
    openssl \
    libgpiod \
    yaml-cpp \
    zlib \
    bluez5 \
    i2c-tools \
    libusb1 \
"

# Systemd configuration
SYSTEMD_SERVICE:${PN} = "meshtasticd.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
FILES:${PN} += "${systemd_unitdir}/system/meshtasticd.service"


CFLAGS:remove = "-fcanon-prefix-map"
CXXFLAGS:remove = "-fcanon-prefix-map"
LDFLAGS:remove = "-fcanon-prefix-map"
CXXFLAGS:prepend = " -isystem ${STAGING_INCDIR}/c++/13.4.0 -isystem ${STAGING_INCDIR}/c++/13.4.0/${TARGET_SYS} -isystem ${STAGING_INCDIR}/c++/13.4.0/backward"

# Add in our build patch. This is necessary because
# platformio isn't picking up some of the yocto linker path locations
SRC_URI += "file://yocto-link-fix.py file://config.yaml file://10-sx1262-usb.yaml file://99-usb-serial.rules"

# Add our custom patch to the platformio build system.
do_configure:append() {
    cp ${WORKDIR}/yocto-link-fix.py ${S}/bin/yocto-link-fix.py
    sed -i "/^\\s*bin\\/platformio-custom.py/a\\        post:bin/yocto-link-fix.py" ${S}/platformio.ini
}

do_compile () {
    CC_BINARY="$(echo ${CC} | awk '{print $1}')"
    CXX_BINARY="$(echo ${CXX} | awk '{print $1}')"

    export TARGET_CC="${CC_BINARY}"
    export TARGET_CXX="${CXX_BINARY}"
    export TARGET_AR="${AR}"
    export TARGET_AS="${AS}"
    export TARGET_LD="${LD}"
    export TARGET_RANLIB="${RANLIB}"
    export TARGET_OBJCOPY="${OBJCOPY}"
    export STAGING_DIR_TARGET="${STAGING_DIR_TARGET}"

    export PLATFORMIO_CORE_DIR="${WORKDIR}/.platformio"
    export PLATFORMIO_CACHE_DIR="${WORKDIR}/.platformio_cache"
    export PLATFORMIO_BUILD_CACHE_DIR="${WORKDIR}/.platformio_build_cache"

    export PLATFORMIO_BUILD_FLAGS="--sysroot=${STAGING_DIR_TARGET} ${CXXFLAGS} ${CFLAGS} -I${STAGING_INCDIR} ${LDFLAGS} -L${STAGING_LIBDIR} -L${STAGING_DIR_TARGET}/lib -B${STAGING_LIBDIR}/${TARGET_SYS}/13.4.0"
    export LDFLAGS="--sysroot=${STAGING_DIR_TARGET} ${LDFLAGS} -L${STAGING_LIBDIR}"

    # There's no yocto environment available in the meshtasticd build system,
    # but we can use the buildroot environment and the path hackery above to
    # successfully build the meshtasticd binary
    ${STAGING_BINDIR_NATIVE}/python3-native/python3 -m platformio run \
        --environment buildroot \
        -v \
        --project-dir ${S}
}

do_install () {
    # Create directories
    install -d ${D}${sysconfdir}/meshtasticd/config.d
    install -d ${D}${sysconfdir}/meshtasticd/available.d

    # Install the meshtasticd binary
    install -D -m 0755 ${S}/.pio/build/buildroot/meshtasticd ${D}${bindir}/meshtasticd
    install -D -m 0755 ${S}/bin/meshtasticd-start.sh ${D}${bindir}/meshtasticd-start.sh

    # Install configuration files
    install -D -m 0644 ${WORKDIR}/config.yaml ${D}${sysconfdir}/meshtasticd/config.yaml
    install -d ${D}${sysconfdir}/meshtasticd/available.d
    cp -r ${S}/bin/config.d/* ${D}${sysconfdir}/meshtasticd/available.d/
    install -D ${WORKDIR}/10-sx1262-usb.yaml ${D}${sysconfdir}/meshtasticd/config.d/10-sx1262-usb.yaml

    # Optionally install service files (choose the proper scheme based on your init system)
    # For a SysV init script:
    #install -d ${D}${sysconfdir}/init.d
    #install -D -m 0755 ${S}/S99meshtasticd ${D}${sysconfdir}/init.d/meshtasticd
    #install -D -m 0755 ${S}/meshtasticd-syslog-wrapper.sh ${D}${libexecdir}/meshtasticd-syslog-wrapper.sh

    # If you are using systemd, you might also install a service file:
    install -d ${D}${systemd_unitdir}/system
    install -D -m 0644 ${S}/bin/meshtasticd.service ${D}${systemd_unitdir}/system/meshtasticd.service

    # Install the device udev rules
    install -d ${D}/etc/udev/rules.d
    install -D -m 0644 ${WORKDIR}/99-usb-serial.rules ${D}/etc/udev/rules.d/99-usb-serial.rules

}
