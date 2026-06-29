package ireader.data.catalog.impl

import com.googlecode.d2j.dex.Dex2jar
import com.googlecode.d2j.reader.MultiDexFileReader
import com.googlecode.dex2jar.tools.BaksmaliBaseDexExceptionHandler
import ireader.core.log.Log
import java.io.File

/**
 * Converts Android APK/DEX files to JAR using the dex2jar toolchain.
 *
 * Used by both [DesktopCatalogLoader] (for IReader extensions) and
 * [DesktopTsundokuExtensionLoader] (for Tsundoku extensions).
 *
 * Adopted from com.googlecode.dex2jar.tools.Dex2jarCmd.doCommandLine.
 * Source: https://github.com/DexPatcher/dex2jar/tree/v2.1-20190905-lanchon
 */
@Suppress("NewApi")
object Dex2JarConverter {

    /**
     * Convert a DEX/APK file to a JAR file.
     *
     * @param dexFile  the source APK or DEX file
     * @param jarFile  the target JAR file (will be overwritten if it exists)
     * @return true if conversion succeeded, false otherwise
     */
    fun convert(dexFile: File, jarFile: File): Boolean {
        return try {
            val reader = MultiDexFileReader.open(dexFile.inputStream())
            val handler = BaksmaliBaseDexExceptionHandler()
            Dex2jar
                .from(reader)
                .withExceptionHandler(handler)
                .reUseReg(false)
                .topoLogicalSort()
                .skipDebug(true)
                .optimizeSynchronized(false)
                .printIR(false)
                .noCode(false)
                .skipExceptions(false)
                .to(jarFile.toPath())
            true
        } catch (e: Exception) {
            Log.error { "Dex2JarConverter: ${e.message}" }
            false
        }
    }
}
