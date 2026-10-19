package ireader.data.chapter

import ireader.core.source.model.Text
import ireader.core.source.model.decode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChapterContentDecodeTest {
    @Test
    fun freeWebNovelPagesRoundTrip() {
        val raw = """[{"type":"ireader.core.source.model.Text","text":"BEEP! BEEP! BEEP!"}]"""
        val pages = raw.decode()
        assertEquals(1, pages.size)
        assertTrue(pages[0] is Text)
        assertEquals("BEEP! BEEP! BEEP!", (pages[0] as Text).text)
    }
}
