package ireader.data.font

import data.CustomFonts
import ireader.core.db.Transactions
import ireader.domain.data.repository.FontRepository
import ireader.domain.models.common.Uri
import ireader.domain.models.fonts.CustomFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import java.util.UUID
import ireader.domain.utils.extensions.currentTimeToLong

val customFontMapper: (
    id: String,
    name: String,
    filePath: String,
    isSystemFont: Boolean,
    dateAdded: Long
) -> CustomFont = { id, name, filePath, isSystemFont, dateAdded ->
    CustomFont(
        id = id,
        name = name,
        filePath = filePath,
        isSystemFont = isSystemFont,
        dateAdded = dateAdded
    )
}

class FontRepositoryImpl(
    private val handler: ireader.data.core.DatabaseHandler,
    private val transactions: Transactions
) : FontRepository {
    private val fileSystem: FileSystem = FileSystem.SYSTEM
    private val fontsDirectory: String = "fonts" // Will be platform-specific

    override suspend fun importFont(filePath: String, fontName: String): Result<CustomFont> {
        return withContext(Dispatchers.IO) {
            try {
                // Handle system fonts (empty filePath) - just insert into database
                if (filePath.isEmpty()) {
                    val fontId = "system_${fontName.lowercase().replace(" ", "_")}"
                    val customFont = CustomFont(
                        id = fontId,
                        name = fontName,
                        filePath = "",
                        isSystemFont = true,
                        dateAdded = currentTimeToLong()
                    )
                    
                    // Save to database
                    handler.await(inTransaction = true) {
                        customFontQueries.insert(
                            id = customFont.id,
                            name = customFont.name,
                            filePath = customFont.filePath,
                            isSystemFont = customFont.isSystemFont,
                            dateAdded = customFont.dateAdded
                        )
                    }
                    
                    return@withContext Result.success(customFont)
                }
                
                val sourcePath = filePath.toPath()
                
                // Validate file exists and is a font file
                if (!fileSystem.exists(sourcePath)) {
                    return@withContext Result.failure(IllegalArgumentException("Font file does not exist"))
                }
                
                val extension = sourcePath.name.substringAfterLast('.', "")
                if (extension !in listOf("ttf", "otf")) {
                    return@withContext Result.failure(IllegalArgumentException("Invalid font file format. Only .ttf and .otf files are supported"))
                }
                
                // Create fonts directory if it doesn't exist
                val fontsDir = fontsDirectory.toPath()
                if (!fileSystem.exists(fontsDir)) {
                    fileSystem.createDirectories(fontsDir)
                }
                
                // Generate unique ID and destination path
                val fontId = UUID.randomUUID().toString()
                val fileName = "${fontId}.${extension}"
                val destPath = fontsDir.resolve(fileName)
                
                // Copy font file to app directory
                fileSystem.copy(sourcePath, destPath)
                
                // Create CustomFont object
                val customFont = CustomFont(
                    id = fontId,
                    name = fontName,
                    filePath = destPath.toString(),
                    isSystemFont = false,
                    dateAdded = currentTimeToLong()
                )
                
                // Save to database
                handler.await(inTransaction = true) {
                    customFontQueries.insert(
                        id = customFont.id,
                        name = customFont.name,
                        filePath = customFont.filePath,
                        isSystemFont = customFont.isSystemFont,
                        dateAdded = customFont.dateAdded
                    )
                }
                
                Result.success(customFont)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getAllFonts(): List<CustomFont> {
        return handler.awaitList {
            customFontQueries.getAllFonts(customFontMapper)
        }
    }

    override suspend fun getCustomFonts(): List<CustomFont> {
        return handler.awaitList {
            customFontQueries.getCustomFonts(customFontMapper)
        }
    }

    override suspend fun getSystemFonts(): List<CustomFont> {
        return handler.awaitList {
            customFontQueries.getSystemFonts(customFontMapper)
        }
    }

    override suspend fun deleteFont(fontId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Get font to delete file
                val font = getFontById(fontId)
                
                if (font == null) {
                    return@withContext Result.failure(IllegalArgumentException("Font not found"))
                }
                
                if (font.isSystemFont) {
                    return@withContext Result.failure(IllegalArgumentException("Cannot delete system fonts"))
                }
                
                // Delete file
                val fontPath = font.filePath.toPath()
                if (fileSystem.exists(fontPath)) {
                    fileSystem.delete(fontPath)
                }
                
                // Delete from database
                handler.await(inTransaction = true) {
                    customFontQueries.delete(fontId)
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getFontById(fontId: String): CustomFont? {
        return handler.awaitOneOrNull {
            customFontQueries.getFontById(fontId, customFontMapper)
        }
    }
    
    override suspend fun importFontFromUri(uri: Uri, fontName: String): Result<CustomFont> {
        return withContext(Dispatchers.IO) {
            try {
                // Determine file extension from URI or default to ttf
                val uriString = uri.toString()
                val extension = when {
                    uriString.contains(".otf", ignoreCase = true) -> "otf"
                    uriString.contains(".ttf", ignoreCase = true) -> "ttf"
                    else -> "ttf" // default
                }
                
                // Create fonts directory if it doesn't exist
                val fontsDir = fontsDirectory.toPath()
                if (!fileSystem.exists(fontsDir)) {
                    fileSystem.createDirectories(fontsDir)
                }
                
                // Generate unique ID and destination path
                val fontId = UUID.randomUUID().toString()
                val fileName = "${fontId}.${extension}"
                val destPath = fontsDir.resolve(fileName)
                
                // Copy font file from URI to app directory using platform-specific helper
                copyFontFromUri(uri, destPath)
                
                // Create CustomFont object
                val customFont = CustomFont(
                    id = fontId,
                    name = fontName,
                    filePath = destPath.toString(),
                    isSystemFont = false,
                    dateAdded = currentTimeToLong()
                )
                
                // Save to database
                handler.await(inTransaction = true) {
                    customFontQueries.insert(
                        id = customFont.id,
                        name = customFont.name,
                        filePath = customFont.filePath,
                        isSystemFont = customFont.isSystemFont,
                        dateAdded = customFont.dateAdded
                    )
                }
                
                Result.success(customFont)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
