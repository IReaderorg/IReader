package ireader.core.io

import java.io.File

/**
 * Migration helpers to convert between java.io.File and VirtualFile.
 * These are temporary utilities to help with gradual migration.
 */

/**
 * Converts a java.io.File to VirtualFile.
 */
fun File.toVirtualFile(): VirtualFile = JvmVirtualFile(this)

/**
 * Converts a VirtualFile to java.io.File (JVM only).
 * Throws IllegalArgumentException if the VirtualFile is not a JvmVirtualFile.
 */
fun VirtualFile.toJavaFile(): File {
    require(this is JvmVirtualFile) { "Can only convert JvmVirtualFile to java.io.File" }
    return this.file
}

/**
 * Creates a VirtualFile from a path string.
 */
fun String.toVirtualFile(): VirtualFile = File(this).toVirtualFile()
