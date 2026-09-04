package ireader.domain.services.tts_service.v2

/**
 * Intelligent sentence and clause segmenter for TTS synthesis.
 *
 * Splitting long paragraphs into natural sentences and clauses prevents
 * synthesis timeouts (such as Android TextToSpeech ERROR_SYNTHESIS code -3,
 * which triggers when a single synthesis exceeds ~25-30 seconds).
 */
object TTSSentenceSplitter {

    /**
     * Default maximum characters per chunk.
     * At normal speech rates (~4-5 characters/second for CJK, ~15 chars/sec for English),
     * 80 characters takes roughly 6 to 18 seconds, well below the 30-second system timeout.
     */
    const val DEFAULT_MAX_CHUNK_LENGTH = 80

    // Sentence terminators: Chinese/Japanese fullwidth + Western punctuation + newline
    private val PRIMARY_TERMINATORS = charArrayOf('。', '！', '？', '\n', '.', '!', '?')

    // Clause separators used when a sentence exceeds maxChunkLength
    private val SECONDARY_SEPARATORS = charArrayOf('，', ',', '；', ';', '：', ':', '—', '…')

    // Closing quotes, brackets, and parenthesis that should stay attached to the preceding sentence
    private val CLOSING_QUOTES = setOf('”', '’', '"', '\'', '）', ')', ']', '】', '」', '』', '》', '>')

    // Common abbreviations that shouldn't trigger a sentence break on period
    private val ABBREVIATIONS = setOf(
        "mr", "mrs", "ms", "dr", "prof", "sr", "jr", "vs", "etc", "ie", "eg", "st"
    )

    /**
     * Splits [text] into smaller, natural chunks suitable for TTS synthesis.
     *
     * @param text The paragraph or text block to segment.
     * @param maxChunkLength Target upper bound for each chunk in characters.
     * @return Ordered list of non-empty chunks.
     */
    fun split(text: String, maxChunkLength: Int = DEFAULT_MAX_CHUNK_LENGTH): List<String> {
        if (text.isBlank()) return emptyList()

        val primarySentences = splitPrimary(text)
        val result = mutableListOf<String>()

        for (sentence in primarySentences) {
            if (sentence.length <= maxChunkLength) {
                result.add(sentence)
            } else {
                // Sentence is too long, sub-split by clauses
                val subChunks = splitSecondary(sentence, maxChunkLength)
                result.addAll(subChunks)
            }
        }

        return result.filter { it.isNotBlank() }
    }

    /**
     * Primary split: Break text at sentence boundaries.
     */
    private fun splitPrimary(text: String): List<String> {
        val sentences = mutableListOf<String>()
        val current = StringBuilder()
        var i = 0

        while (i < text.length) {
            val c = text[i]
            current.append(c)

            if (c in PRIMARY_TERMINATORS) {
                // Check for decimal numbers (e.g. 3.14)
                if (c == '.' && isDecimalNumber(text, i)) {
                    i++
                    continue
                }

                // Check for abbreviations (e.g. Mr. Smith)
                if (c == '.' && isAbbreviation(text, i)) {
                    i++
                    continue
                }

                // Check for ellipsis (... or ……)
                if (c == '.' && isEllipsis(text, i)) {
                    i++
                    continue
                }

                // Absorb any immediately following closing quotes/brackets/punctuation
                while (i + 1 < text.length && (text[i + 1] in CLOSING_QUOTES || text[i + 1] in PRIMARY_TERMINATORS)) {
                    i++
                    current.append(text[i])
                }

                val sentence = current.toString().trim()
                if (sentence.isNotEmpty()) {
                    sentences.add(sentence)
                }
                current.clear()
            }
            i++
        }

        val remaining = current.toString().trim()
        if (remaining.isNotEmpty()) {
            sentences.add(remaining)
        }

        return sentences
    }

    /**
     * Secondary split: Sub-divide a long sentence at clause separators or spaces.
     */
    private fun splitSecondary(sentence: String, maxChunkLength: Int): List<String> {
        val clauses = extractClauses(sentence)
        val result = mutableListOf<String>()
        val current = StringBuilder()

        for (clause in clauses) {
            if (current.isNotEmpty() && current.length + clause.length > maxChunkLength) {
                val chunk = current.toString().trim()
                if (chunk.isNotEmpty()) {
                    result.add(chunk)
                }
                current.clear()
            }

            if (clause.length > maxChunkLength) {
                // Clause itself exceeds maxChunkLength, split by words or hard boundary
                val subPieces = splitHard(clause, maxChunkLength)
                for (i in 0 until subPieces.size - 1) {
                    result.add(subPieces[i])
                }
                current.append(subPieces.last())
            } else {
                current.append(clause)
            }
        }

        val remaining = current.toString().trim()
        if (remaining.isNotEmpty()) {
            result.add(remaining)
        }

        return result
    }

    /**
     * Break sentence into clause tokens based on secondary punctuation.
     */
    private fun extractClauses(sentence: String): List<String> {
        val clauses = mutableListOf<String>()
        val current = StringBuilder()
        var i = 0

        while (i < sentence.length) {
            val c = sentence[i]
            current.append(c)

            if (c in SECONDARY_SEPARATORS) {
                // Absorb trailing closing quotes or consecutive separators
                while (i + 1 < sentence.length && (sentence[i + 1] in CLOSING_QUOTES || sentence[i + 1] in SECONDARY_SEPARATORS)) {
                    i++
                    current.append(sentence[i])
                }
                val token = current.toString()
                if (token.isNotEmpty()) {
                    clauses.add(token)
                }
                current.clear()
            }
            i++
        }

        val remaining = current.toString()
        if (remaining.isNotEmpty()) {
            clauses.add(remaining)
        }

        return clauses
    }

    /**
     * Hard-split a clause that has no internal clause punctuation.
     * Prefers whitespace boundaries when available; otherwise splits at maxChunkLength.
     */
    private fun splitHard(text: String, maxChunkLength: Int): List<String> {
        val pieces = mutableListOf<String>()
        var remaining = text

        while (remaining.length > maxChunkLength) {
            val candidate = remaining.substring(0, maxChunkLength)
            val lastSpace = candidate.lastIndexOf(' ')

            val splitPoint = if (lastSpace > maxChunkLength / 3) {
                lastSpace + 1
            } else {
                maxChunkLength
            }

            val piece = remaining.substring(0, splitPoint).trim()
            if (piece.isNotEmpty()) {
                pieces.add(piece)
            }
            remaining = remaining.substring(splitPoint).trimStart()
        }

        if (remaining.isNotEmpty()) {
            pieces.add(remaining.trim())
        }

        return pieces
    }

    private fun isDecimalNumber(text: String, index: Int): Boolean {
        val prev = text.getOrNull(index - 1)
        val next = text.getOrNull(index + 1)
        return prev != null && next != null && prev.isDigit() && next.isDigit()
    }

    private fun isAbbreviation(text: String, index: Int): Boolean {
        // Look back for previous word characters
        var start = index - 1
        while (start >= 0 && text[start].isLetter()) {
            start--
        }
        val word = text.substring(start + 1, index).lowercase()
        return word in ABBREVIATIONS
    }

    private fun isEllipsis(text: String, index: Int): Boolean {
        val next = text.getOrNull(index + 1)
        val prev = text.getOrNull(index - 1)
        return next == '.' || prev == '.'
    }
}
