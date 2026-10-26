package ireader.data.catalog.js

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsRuntimeHostTest {

    @Test
    fun preludeDefinesFetchAndTimers() {
        val script = JsRuntimeHost.prelude()

        assertTrue(script.contains("globalThis.fetch"))
        assertTrue(script.contains("globalThis.setTimeout"))
        assertTrue(script.contains("__fetchQueue"))
        assertTrue(script.contains("__deliverFetch"))
        assertTrue(script.contains("__drainTimers"))
        assertFalse(script.contains("setTimeout(checkResult"))
    }

    @Test
    fun invokeWaitsForPromiseInsteadOfStringifyingIt() {
        val script = JsRuntimeHost.invoke("SourceBridge.search('freewebnovel', 'dao', 1)", "[]")

        assertTrue(script.contains(".then("))
        assertTrue(script.contains("SourceBridge.search('freewebnovel', 'dao', 1)"))
        assertFalse(script.contains("JSON.stringify(SourceBridge"))
    }

    @Test
    fun searchCallTargetsSourceBridgeAndRegistry() {
        val script = JsRuntimeHost.searchCall(
            sourceId = "ireader.freewebnovel.en",
            registryId = "freewebnovel",
            query = """dao"s""",
            page = 2,
        )

        assertTrue(script.contains("SourceBridge.search"))
        assertTrue(script.contains("getSource("))
        assertTrue(script.contains("freewebnovel"))
        assertTrue(script.contains(".then("))
        assertFalse(script.contains("JSON.stringify(SourceBridge"))
        assertFalse(script.contains("dao\"s"))
    }

    @Test
    fun deliverFetchEscapesResponseBody() {
        val script = JsRuntimeHost.deliverFetch(
            id = 3,
            status = 200,
            url = "https://freewebnovel.com/novel/dao",
            body = """He said "hello"""",
            headers = mapOf("content-type" to "text/html"),
        )

        assertTrue(script.contains("__deliverFetch("))
        assertTrue(script.contains("\\\"hello\\\""))
        assertTrue(script.contains("content-type"))
    }

    @Test
    fun parseFetchQueueReadsRequest() {
        val requests = JsRuntimeHost.parseFetchQueue(
            """[{"id":7,"url":"https://freewebnovel.com/sort/latest-release","method":"GET","body":"","headers":{"User-Agent":"ios"}}]"""
        )

        assertEquals(1, requests.size)
        assertEquals(7, requests[0].id)
        assertEquals("https://freewebnovel.com/sort/latest-release", requests[0].url)
        assertEquals("GET", requests[0].method)
        assertEquals("ios", requests[0].headers["User-Agent"])
        assertEquals(emptyList(), JsRuntimeHost.parseFetchQueue("not-json"))
    }

    @Test
    fun registryIdUsesPackageSegment() {
        assertEquals("freewebnovel", JsRuntimeHost.registryId("ireader.freewebnovel.en", "FreeWebNovel"))
    }

    @Test
    fun registryShimAdaptsTwoArgumentRegister() {
        val script = JsRuntimeHost.registryShim("initFreeWebNovel")

        assertTrue(script.contains("typeof name === 'function'"))
        assertTrue(script.contains("initFreeWebNovel"))
        assertTrue(script.contains("IReaderSources"))
    }
}
