package ireader.domain.usecases.epub

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.TextNode
import ireader.core.source.model.Text

/**
 * Robust text extractor for EPUB chapter HTML/XHTML documents.
 * 
 * Accurately extracts chapter paragraphs while:
 * - Preserving word spacing across inline tags (<b>, <i>, <em>, <strong>, <span>, <a>)
 * - Properly converting <br> tags into line / paragraph breaks
 * - Handling standard block elements (<p>, <h1>..<h6>, <blockquote>, <pre>, <li>)
 * - Handling container <div> and <body> structures with mixed text nodes and elements
 * - Avoiding empty paragraphs
 */
object EpubTextExtractor {

    private const val BR_PLACEHOLDER = "___IREADER_BR_BREAK___"

    private val LEAF_BLOCK_TAGS = setOf(
        "p", "h1", "h2", "h3", "h4", "h5", "h6",
        "blockquote", "pre", "li", "dd", "dt"
    )

    private val CONTAINER_TAGS = setOf(
        "body", "div", "section", "article", "main",
        "header", "footer", "nav", "aside"
    )

    fun extractTextContent(doc: Document): List<Text> {
        val body = doc.body()

        // Replace <br> tags with placeholder so line breaks survive text normalization
        doc.select("br").forEach { br ->
            br.replaceWith(TextNode(BR_PLACEHOLDER))
        }

        val textList = mutableListOf<Text>()

        fun isContainer(element: Element): Boolean {
            val tag = element.tagName().lowercase()
            if (tag in LEAF_BLOCK_TAGS) return false
            if (tag in CONTAINER_TAGS) {
                // If it contains any child block element, treat as container
                return element.children().any { child ->
                    val childTag = child.tagName().lowercase()
                    childTag in LEAF_BLOCK_TAGS || childTag in CONTAINER_TAGS
                }
            }
            return false
        }

        fun isLeafBlock(element: Element): Boolean {
            val tag = element.tagName().lowercase()
            if (tag in LEAF_BLOCK_TAGS) return true
            if (tag in CONTAINER_TAGS) {
                // A div/section without any child block elements is a leaf block (e.g. <div>text</div>)
                return !element.children().any { child ->
                    val childTag = child.tagName().lowercase()
                    childTag in LEAF_BLOCK_TAGS || childTag in CONTAINER_TAGS
                }
            }
            return false
        }

        fun emitParagraph(rawText: String) {
            val lines = rawText.split(BR_PLACEHOLDER)
            for (line in lines) {
                val cleaned = line.trim()
                if (cleaned.isNotBlank()) {
                    textList.add(Text(cleaned))
                }
            }
        }

        fun processContainer(container: Element) {
            val inlineBuffer = mutableListOf<String>()

            fun flushInlineBuffer() {
                if (inlineBuffer.isNotEmpty()) {
                    val combined = inlineBuffer.joinToString(" ").trim()
                    inlineBuffer.clear()
                    if (combined.isNotBlank()) {
                        emitParagraph(combined)
                    }
                }
            }

            container.childNodes().forEach { node ->
                when (node) {
                    is TextNode -> {
                        val text = node.text().trim()
                        if (text.isNotBlank()) {
                            inlineBuffer.add(text)
                        }
                    }
                    is Element -> {
                        if (isContainer(node)) {
                            flushInlineBuffer()
                            processContainer(node)
                        } else if (isLeafBlock(node)) {
                            flushInlineBuffer()
                            if (node.tagName().lowercase() == "pre") {
                                node.wholeText().split("\n").forEach { emitParagraph(it) }
                            } else {
                                emitParagraph(node.text())
                            }
                        } else {
                            // Inline element (span, i, b, a, em, etc.)
                            val text = node.text().trim()
                            if (text.isNotBlank()) {
                                inlineBuffer.add(text)
                            }
                        }
                    }
                }
            }
            flushInlineBuffer()
        }

        processContainer(body)

        return textList.ifEmpty {
            val bodyText = body.text().trim()
            if (bodyText.isNotBlank()) {
                val lines = bodyText.split(BR_PLACEHOLDER)
                lines.mapNotNull { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotBlank()) Text(trimmed) else null
                }
            } else {
                emptyList()
            }
        }
    }
}
