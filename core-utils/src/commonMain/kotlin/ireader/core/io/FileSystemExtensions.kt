package ireader.core.io

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

/**
 * Extension functions for VirtualFile to provide convenient utilities.
 */

/**
 * Checks if the file has the given extension (case-insensitive).
 */
fun VirtualFile.hasExtension(ext: String): Boolean {
    return extension.equals(ext, ignoreCase = true)
}

/**
 * Ensures the parent directory exists.
 */
suspend fun VirtualFile.ensureParentExists(): Boolean {
    return parent?.mkdirs() ?: false
}

/**
 * Reads lines from the file.
 */
suspend fun VirtualFile.readLines(): List<String> {
    return readText().lines()
}

/**
 * Writes lines to the file.
 */
suspend fun VirtualFile.writeLines(lines: List<String>) {
    writeText(lines.joinToString("\n"))
}

/**
 * Checks if the file is empty.
 */
suspend fun VirtualFile.isEmpty(): Boolean {
    return size() == 0L
}

/**
 * Gets the file name without extension.
 */
val VirtualFile.nameWithoutExtension: String
    get() = name.substringBeforeLast(".")

/**
 * Walks the file tree with a filter.
 */
fun VirtualFile.walkFiltered(filter: (VirtualFile) -> Boolean): Flow<VirtualFile> {
    return walk().filter(filter)
}

/**
 * Copies the file to a directory.
 */
suspend fun VirtualFile.copyToDirectory(targetDir: VirtualFile, overwrite: Boolean = false): VirtualFile {
    val target = targetDir.resolve(name)
    copyTo(target, overwrite)
    return target
}

/**
 * Moves the file to a directory.
 */
suspend fun VirtualFile.moveToDirectory(targetDir: VirtualFile): VirtualFile {
    val target = targetDir.resolve(name)
    moveTo(target)
    return target
}

/**
 * Creates the file if it doesn't exist.
 */
suspend fun VirtualFile.createIfNotExists(): Boolean {
    return if (!exists()) {
        ensureParentExists()
        createNewFile()
    } else {
        true
    }
}

/**
 * Deletes the file if it exists.
 */
suspend fun VirtualFile.deleteIfExists(): Boolean {
    return if (exists()) delete() else true
}

/**
 * Gets the relative path from this file to another.
 */
fun VirtualFile.relativeTo(base: VirtualFile): String {
    return path.removePrefix(base.path).removePrefix("/")
}
