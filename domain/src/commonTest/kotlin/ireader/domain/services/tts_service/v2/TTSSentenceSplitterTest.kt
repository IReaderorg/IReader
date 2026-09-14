package ireader.domain.services.tts_service.v2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TTSSentenceSplitterTest {

    @Test
    fun `empty and blank strings return empty list`() {
        assertEquals(emptyList(), TTSSentenceSplitter.split(""))
        assertEquals(emptyList(), TTSSentenceSplitter.split("   \n\t  "))
    }

    @Test
    fun `short text without punctuation returns single item`() {
        val text = "Hello world"
        val result = TTSSentenceSplitter.split(text)
        assertEquals(listOf("Hello world"), result)
    }

    @Test
    fun `splits standard Chinese sentences at terminal punctuation`() {
        val text = "这是第一句话。这是第二句话！这是第三句话？"
        val result = TTSSentenceSplitter.split(text)
        assertEquals(
            listOf(
                "这是第一句话。",
                "这是第二句话！",
                "这是第三句话？"
            ),
            result
        )
    }

    @Test
    fun `splits standard English sentences at terminal punctuation`() {
        val text = "This is the first sentence. This is the second sentence! Is this the third?"
        val result = TTSSentenceSplitter.split(text)
        assertEquals(
            listOf(
                "This is the first sentence.",
                "This is the second sentence!",
                "Is this the third?"
            ),
            result
        )
    }

    @Test
    fun `attaches closing quotes and brackets to preceding sentence`() {
        val text = "“你真的要走吗？”他问道。“是的，我已经决定了！”"
        val result = TTSSentenceSplitter.split(text)
        assertEquals(
            listOf(
                "“你真的要走吗？”",
                "他问道。",
                "“是的，我已经决定了！”"
            ),
            result
        )
    }

    @Test
    fun `does not split decimal numbers or abbreviations`() {
        val text = "Dr. Watson observed that pi is approximately 3.14159. Mr. Holmes agreed."
        val result = TTSSentenceSplitter.split(text)
        assertEquals(
            listOf(
                "Dr. Watson observed that pi is approximately 3.14159.",
                "Mr. Holmes agreed."
            ),
            result
        )
    }

    @Test
    fun `sub-splits long sentences on clause punctuation when exceeding maxChunkLength`() {
        val longSentence = "这是一个非常长的句子，包含多个从句和逗号，用来测试当句子超过指定的最大长度时，分词器是否会自然地在逗号处切分，而不是任由其超长导致合成超时。"
        assertTrue(longSentence.length > 50)

        val result = TTSSentenceSplitter.split(longSentence, maxChunkLength = 30)

        // All chunks must be <= 35 characters
        for (chunk in result) {
            assertTrue(chunk.length <= 35, "Chunk too long: '$chunk' (${chunk.length} chars)")
        }
        // Total text should be preserved
        val combined = result.joinToString("")
        assertEquals(longSentence, combined)
    }

    @Test
    fun `splits user reported 198-character Chinese paragraph into safe chunks under 80 characters`() {
        val userText = "“一位真正的作家永远只为内心写作，只有内心才会真实地告诉他，他的自私、他的高尚是多么突出。内心让他真实地了解自己，一旦了解了自己也就了解了世界。很多年前我就明白了这个原则，可是要捍卫这个原则必须付出艰辛的劳动和长时期的痛苦，因为内心并非时时刻刻都是敞开的，它更多的时候倒是封闭起来，于是只有写作，不停地写作才能使内心敞开，才能使自己置身于发现之中，就像日出的光芒照亮了黑暗，灵感这时候才会突然来到。”"
        assertEquals(200, userText.length)

        val chunks = TTSSentenceSplitter.split(userText, maxChunkLength = 80)

        // Verify multiple chunks created
        assertTrue(chunks.size >= 4, "Expected at least 4 chunks, got ${chunks.size}: $chunks")

        // Verify every single chunk is under 80 characters (prevents 30s timeout)
        for (chunk in chunks) {
            assertTrue(
                chunk.length <= 80,
                "Chunk exceeded max length 80: length=${chunk.length}, content='$chunk'"
            )
            assertTrue(chunk.isNotBlank(), "Chunk should not be blank")
        }

        // Verify all content is preserved
        val reassembled = chunks.joinToString("")
        assertEquals(userText, reassembled)
    }

    @Test
    fun `solitary punctuation strings return empty list`() {
        assertEquals(emptyList(), TTSSentenceSplitter.split("."))
        assertEquals(emptyList(), TTSSentenceSplitter.split("..."))
        assertEquals(emptyList(), TTSSentenceSplitter.split("؟"))
        assertEquals(emptyList(), TTSSentenceSplitter.split("« »"))
        assertEquals(emptyList(), TTSSentenceSplitter.split("!؟ ..."))
        assertEquals(emptyList(), TTSSentenceSplitter.split("، ؛"))
    }

    @Test
    fun `trailing punctuation marks are attached to preceding sentence`() {
        val text = "این یک جمله کامل است. ..."
        val result = TTSSentenceSplitter.split(text)
        assertEquals(listOf("این یک جمله کامل است. ..."), result)
    }

    @Test
    fun `splits Persian sentences with Persian punctuation properly`() {
        val text = "آیا شما به مهمانی می‌آیید؟ بله، من حتماً می‌آیم! «خیلی خوب است»"
        val result = TTSSentenceSplitter.split(text)
        assertEquals(
            listOf(
                "آیا شما به مهمانی می‌آیید؟",
                "بله، من حتماً می‌آیم!",
                "«خیلی خوب است»"
            ),
            result
        )
    }

    @Test
    fun `does not split normal English sentences between 80 and 350 characters mid-sentence`() {
        // A typical English sentence with ~200 characters that previously was chopped at 80 characters, causing awkward pauses
        val sentence = "He walked slowly into the bustling marketplace, observing the vibrant colors of the merchant stalls, while wondering if he would ever manage to find what he had traveled so far across the realm to discover."
        assertTrue(sentence.length in 150..300, "Sentence length: ${sentence.length}")

        val result = TTSSentenceSplitter.split(sentence)

        // Must remain a single sentence and not be split mid-sentence at commas
        assertEquals(listOf(sentence), result)
    }

    @Test
    fun `sub-splits English sentences exceeding 350 characters at clause boundaries`() {
        val veryLongSentence = "He walked slowly into the bustling marketplace, observing the vibrant colors of the merchant stalls and the noisy chatter of the crowd, while wondering whether he would ever manage to find what he had traveled so far across the realm to discover, because time was quickly running out and the dark clouds gathering on the horizon suggested that a storm was approaching fast."
        assertTrue(veryLongSentence.length > 350, "Sentence length: ${veryLongSentence.length}")

        val result = TTSSentenceSplitter.split(veryLongSentence)

        assertTrue(result.size > 1, "Expected sentence over 350 chars to be split into clauses")
        for (chunk in result) {
            assertTrue(chunk.length <= 350, "Chunk exceeded max length 350: '$chunk'")
        }
    }

    @Test
    fun `CJK text automatically defaults to 100 character threshold`() {
        val longCjkSentence = "在很久很久以前的一个古老王国里，住着一位非常聪明的学者，他每天都在图书馆里研读古籍，试图寻找隐藏在历史长河中的秘密，以便能够帮助王国的百姓解决困扰他们已久的干旱和饥荒问题，然而这个秘密却被深深地掩埋在时间的尘埃中。"
        assertTrue(longCjkSentence.length > 100, "Sentence length: ${longCjkSentence.length}")

        // When maxChunkLength is omitted (defaults to -1), auto-detection applies CJK_MAX_CHUNK_LENGTH (100)
        val result = TTSSentenceSplitter.split(longCjkSentence)

        assertTrue(result.size > 1, "Expected CJK sentence over 100 chars to be split")
        for (chunk in result) {
            assertTrue(chunk.length <= 100, "CJK chunk exceeded max length 100: '$chunk'")
        }
    }
}

