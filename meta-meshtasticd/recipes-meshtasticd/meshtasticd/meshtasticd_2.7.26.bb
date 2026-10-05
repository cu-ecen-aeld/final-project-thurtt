###############################################################################
#
# This recipe is adapted from https://github.com/buildroot-meshtastic/meta-meshtastic
# License coveys from the original repo and is included in this recipe
#
###############################################################################

inherit python3native
inherit systemd

SUMMARY = "meshtasticd firmware daemon"
DESCRIPTION = "meshtasticd is the firmware daemon for Meshtastic"
HOMEPAGE = "https://github.com/meshtastic/firmware"
LICENSE = "GPL-3.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8f0e2cd40e05189ec81232da84bd6e1a"

SRC_URI = "gitsm://github.com/meshtastic/firmware;protocol=https;branch=2.7"
SRC_URI[sha256sum] = "3977fb33d30835f4fcde290a5ef88d00affca2a5dcf5415555adff3e3b39336a"
SRCREV = "54e0d8d0ab2ff56b3a9ce967e53f79e49af560fb"

# Download the web ui source. This is handy for configuring the radio
SRC_URI += "https://github.com/meshtastic/web/releases/download/v2.7.1/build.tar;name=webui;unpack=0"
SRC_URI[webui.sha256sum] = "dfb36b72f092d6e8cd0f202c0c4c01c4742dd3feb49758c574727f251299dbfe"

# platformio insists on downloading packages during the compiliation process
do_compile[network] = "1"

# Where to find the source once fetched
S = "${WORKDIR}/git"

DEPENDS += " \
    pkgconf \
    zlib \
    openssl-native \
    python3-native \
    python3-platformio-native \
    libgpiod yaml-cpp bluez5 i2c-tools libusb1 libbsd libuv \
    orcania yder ulfius libmicrohttpd gnutls jansson \
"

RDEPENDS:${PN} += " \
    openssl \
    libgpiod \
    yaml-cpp \
    zlib \
    bluez5 \
    i2c-tools \
    libusb1 \
    orcania \
    yder \
    ulfius \
    libmicrohttpd \
    gnutls \
    jansson \
    mesh-statusd \
"

inherit systemd

SYSTEMD_SERVICE:${PN} = "meshtasticd.service configure_radio.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

# Add users
inherit useradd

USERADD_PACKAGES = "${PN}"

GROUPADD_PARAM:${PN} = "--system gpio; --system spi; --system i2c; --system dialout"
USERADD_PARAM:${PN} = "--system --create-home --groups gpio,spi,i2c,dialout --user-group meshtasticd"

CFLAGS:remove = "-fcanon-prefix-map"
CXXFLAGS:remove = "-fcanon-prefix-map"
LDFLAGS:remove = "-fcanon-prefix-map"
CXXFLAGS:prepend = " -isystem ${STAGING_INCDIR}/c++/13.4.0 -isystem ${STAGING_INCDIR}/c++/13.4.0/${TARGET_SYS} -isystem ${STAGING_INCDIR}/c++/13.4.0/backward"

# Add in our build patch. This is necessary because
# platformio isn't picking up some of the yocto linker path locations
SRC_URI += "file://yocto-link-fix.py file://config.yaml file://98-gpio-pin.rules file://99-usb-serial.rules file://99-i2c.rules file://configure_radio.sh file://configure_radio.service"

do_configure:append() {
    # Due to the way that platformio assembles the linker parameters, we need this hack
    # To add our own custom linker libraries. There's likely a better way to do this.
    cp ${WORKDIR}/yocto-link-fix.py ${S}/bin/yocto-link-fix.py
    sed -i "/^\\s*bin\\/platformio-custom.py/a\\        post:bin/yocto-link-fix.py" ${S}/platformio.ini
    sed -i "/-lgpiod/a\\    -lulfius\n    -lorcania\n    -lssl\n    -lcrypto" ${S}/variants/native/portduino.ini
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

    # Install the radio configuration script
    install -D -m 0755 ${WORKDIR}/configure_radio.sh ${D}${bindir}/configure_radio.sh

    # Install configuration files
    install -D -m 0644 ${WORKDIR}/config.yaml ${D}${sysconfdir}/meshtasticd/config.yaml
    install -d ${D}${sysconfdir}/meshtasticd/available.d
    cp -r ${S}/bin/config.d/* ${D}${sysconfdir}/meshtasticd/available.d/
    cp ${S}/bin/config.d/lora-Adafruit-RFM9x.yaml ${D}${sysconfdir}/meshtasticd/config.d/

    # Optionally install service files (choose the proper scheme based on your init system)
    # For a SysV init script:
    #install -d ${D}${sysconfdir}/init.d
    #install -D -m 0755 ${S}/S99meshtasticd ${D}${sysconfdir}/init.d/meshtasticd
    #install -D -m 0755 ${S}/meshtasticd-syslog-wrapper.sh ${D}${libexecdir}/meshtasticd-syslog-wrapper.sh

    # If you are using systemd, you might also install a service file:
    install -d ${D}${systemd_unitdir}/system
    install -D -m 0644 ${S}/bin/meshtasticd.service ${D}${systemd_unitdir}/system/meshtasticd.service
    install -D -m 0644 ${WORKDIR}/configure_radio.service ${D}${systemd_unitdir}/system/configure_radio.service

    # Install the device udev rules
    install -d ${D}/etc/udev/rules.d
    install -D -m 0644 ${WORKDIR}/99-usb-serial.rules ${D}/etc/udev/rules.d/99-usb-serial.rules
    install -D -m 0644 ${WORKDIR}/98-gpio-pin.rules ${D}/etc/udev/rules.d/98-gpio-pin.rules
    install -D -m 0644 ${WORKDIR}/99-i2c.rules ${D}/etc/udev/rules.d/99-i2c.rules

    # Web UI assets
    install -m 0775 -o root -g meshtasticd -d ${D}${sysconfdir}/meshtasticd/ssl
    install -m 0775 -o root -g meshtasticd -d -d ${D}${datadir}/meshtasticd/web
    tar -xf ${WORKDIR}/build.tar -C ${D}${datadir}/meshtasticd/web
    gunzip ${D}${datadir}/meshtasticd/web/*.gz
    chown -R root:meshtasticd ${D}${datadir}/meshtasticd/web

}
