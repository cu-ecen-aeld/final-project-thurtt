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

SRC_URI = "gitsm://github.com/meshtastic/firmware;protocol=https"
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

do_compile () {
    CC_BINARY="$(echo ${CC} | awk '{print $1}')"
    CXX_BINARY="$(echo ${CXX} | awk '{print $1}')"
    LDFLAGS="-lbsd"

    export TARGET_CC="${CC_BINARY}"
    export TARGET_CXX="${CXX_BINARY}"
    export TARGET_AR="${AR}"
    export TARGET_AS="${AS}"
    export TARGET_LD="${LD}"
    export TARGET_RANLIB="${RANLIB}"
    export TARGET_OBJCOPY="${OBJCOPY}"

    export PLATFORMIO_CORE_DIR="${WORKDIR}/.platformio"
    export PLATFORMIO_CACHE_DIR="${WORKDIR}/.platformio_cache"
    export PLATFORMIO_BUILD_CACHE_DIR="${WORKDIR}/.platformio_build_cache"

    export PLATFORMIO_BUILD_FLAGS="${CFLAGS} ${CXXFLAGS} -I --sysroot=${STAGING_DIR_TARGET} -I${STAGING_INCDIR}"

    # 4. Invoke PlatformIO build
    ${STAGING_BINDIR_NATIVE}/python3-native/python3 -m platformio run \
        --environment buildroot \
        -v \
        --project-dir ${S}
}

do_install () {
    # Create directories
    install -d ${D}${sbindir}
    install -d ${D}${sysconfdir}/meshtasticd/config.d
    install -d ${D}${sysconfdir}/meshtasticd/available.d

    # Install the meshtasticd binary
    install -D -m 0755 ${S}/.pio/build/native/program ${D}${sbindir}/meshtasticd

    # Install configuration files
    install -D -m 0644 ${S}/bin/config-dist.yaml ${D}${sysconfdir}/meshtasticd/config.yaml
    install -d ${D}${sysconfdir}/meshtasticd/available.d
    cp -r ${S}/bin/config.d/* ${D}${sysconfdir}/meshtasticd/available.d/
    cp -r ${S}/config.d/* ${D}${sysconfdir}/meshtasticd/available.d/

    # Optionally install service files (choose the proper scheme based on your init system)
    # For a SysV init script:
    install -d ${D}${sysconfdir}/init.d
    install -D -m 0755 ${S}/S99meshtasticd ${D}${sysconfdir}/init.d/meshtasticd
    install -D -m 0755 ${S}/meshtasticd-syslog-wrapper.sh ${D}${libexecdir}/meshtasticd-syslog-wrapper.sh

    # If you are using systemd, you might also install a service file:
    # install -d ${D}${systemd_unitdir}/system
    # install -D -m 0644 ${S}/meshtasticd.service ${D}${systemd_unitdir}/system/meshtasticd.service
}
