package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_data.repository.FontRepository
import org.ireader.common_models.entities.FontEntity
import org.ireader.data.local.dao.FontDao

class FontEntityRepositoryImpl(
    private val fontDao: FontDao
) : FontRepository {
    override suspend fun findFontByName(fontName: String): FontEntity? {
        return fontDao.findFontEntity(fontName)
    }

    override fun subscribeFontEntity(): Flow<List<FontEntity>> {
        return fontDao.subscribeFontEntities()
    }

    override suspend fun findAllFontEntities(): List<FontEntity> {
        return fontDao.findAllFontEntities()
    }

    override suspend fun insertFont(fontEntity: FontEntity): Long {
        return fontDao.insert(fontEntity)
    }

    override suspend fun insertFonts(fontEntity: List<FontEntity>): List<Long> {
        return fontDao.insert(fontEntity)
    }

    override suspend fun deleteFonts(fonts: List<FontEntity>) {
        return fontDao.delete(fonts)
    }

    override suspend fun deleteAllFonts() {
        return fontDao.deleteAllFonts()
    }
}