package org.ireader.app

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.assertEquals
import org.ireader.core_api.source.Dependencies
import org.ireader.core_api.source.model.MangaInfo
import org.junit.Test

class ExtensionTests {


    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    //@Test uncomment this line for testing
    fun `check extension`() {
        kotlinx.coroutines.runBlocking {
            val dependencies = Dependencies(
                FakeHttpClients(),
                FakePreferencesStore()
            )
            val result =  KissNovel(dependencies).getChapterList(MangaInfo(key = "https://1stkissnovel.love/novel/princess-is-glamorous-in-modern-day/", title = "Princess is Glamorous in Modern Day"), emptyList())
            print(result)
            assertThat(result.isNotEmpty()).isTrue()
        }
    }
}