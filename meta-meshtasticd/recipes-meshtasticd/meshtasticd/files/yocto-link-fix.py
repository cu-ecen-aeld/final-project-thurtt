Import("env")
Import("projenv")

sysroot_flag = "--sysroot=" + env["ENV"]["STAGING_DIR_TARGET"]
env.Append(LINKFLAGS=[sysroot_flag])
projenv.Append(LINKFLAGS=[sysroot_flag])
