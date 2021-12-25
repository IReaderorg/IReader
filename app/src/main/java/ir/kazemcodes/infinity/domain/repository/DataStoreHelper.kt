package ir.kazemcodes.infinity.domain.repository

import kotlinx.coroutines.flow.Flow


interface DataStoreHelper {
    suspend fun saveSelectedFontState(fontIndex: Int)

    fun readSelectedFontState(): Flow<Int>

    suspend fun saveFontSizeState(fontSize: Int)

    fun readFontSizeState(): Flow<Int>

    suspend fun saveBrightnessState(brightness: Float)

    fun readBrightnessState(): Flow<Float>

    suspend fun saveLatestChapterUseCase(latestChapter: String)

    fun readLatestChapterUseCase(): Flow<String>


}

