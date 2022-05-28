package org.ireader.domain.use_cases.fonts

import com.google.gson.Gson
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import org.ireader.common_data.repository.FontRepository
import org.ireader.common_models.entities.FontEntity
import org.ireader.common_models.fonts.FontResource
import org.ireader.common_resources.REPO_URL
import org.ireader.core_api.http.HttpClients
import javax.inject.Inject

class FontUseCase @Inject constructor(
    private val fontRepository: FontRepository,
    private val clients: HttpClients,
) {

    suspend fun findFontByName(fontName: String): FontEntity? {
        return fontRepository.findFontByName(fontName)
    }

    suspend fun findAllFontEntities(): List<FontEntity> {
        return fontRepository.findAllFontEntities()
    }

    fun subscribeFontEntity(): Flow<List<FontEntity>> {
        return fontRepository.subscribeFontEntity()
    }

    suspend fun insertFont(fontEntity: FontEntity): Long {
        return fontRepository.insertFont(fontEntity)
    }

    suspend fun insertFonts(fontEntity: List<FontEntity>): List<Long> {
        return fontRepository.insertFonts(fontEntity)
    }

    suspend fun deleteFonts(fonts: List<FontEntity>) {
        return fontRepository.deleteFonts(fonts)
    }

    suspend fun deleteAllFonts() {
        return fontRepository.deleteAllFonts()
    }


    suspend fun getRemoteFonts() : FontResource {
        val json : String = clients.default.get("${REPO_URL}/main/fonts.min.json", block = {}).body()
        return Gson().fromJson(json,FontResource::class.java)
    }




}