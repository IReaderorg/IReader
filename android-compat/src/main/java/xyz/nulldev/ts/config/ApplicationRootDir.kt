package xyz.nulldev.ts.config

import java.io.File

val ApplicationRootDir: String = System.getProperty("ireader.root.dir")
    ?: File(System.getProperty("user.dir") ?: ".").absolutePath
