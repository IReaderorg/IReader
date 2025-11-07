package ireader.data.translation

import ireader.core.source.model.Page
import ireader.data.chapter.chapterContentConvertor
import ireader.domain.models.entities.TranslatedChapter

val translatedChapterMapper = { _id: Long,
                                 chapter_id: Long,
                                 book_id: Long,
                                 source_language: String,
                                 target_language: String,
                                 translator_engine_id: Long,
                                 translated_content: String,
                                 created_at: Long,
                                 updated_at: Long ->
    TranslatedChapter(
        id = _id,
        chapterId = chapter_id,
        bookId = book_id,
        sourceLanguage = source_language,
        targetLanguage = target_language,
        translatorEngineId = translator_engine_id,
        translatedContent = chapterContentConvertor.decode(translated_content),
        createdAt = created_at,
        updatedAt = updated_at
    )
}
