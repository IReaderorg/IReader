package ireader.domain.js.library

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.select.Elements

/**
 * Cross-platform Cheerio API for HTML parsing using Ksoup (Kotlin Multiplatform).
 * Provides a cheerio-compatible interface for HTML parsing.
 */
class JSCheerioApi(private val pluginId: String) {
    
    fun load(html: String): CheerioObject {
        val doc = Ksoup.parse(html)
        return CheerioObject(doc, doc.select("*"))
    }

    class CheerioObject(
        private val doc: Document,
        private val elements: Elements
    ) {
        operator fun invoke(selector: String): CheerioObject {
            val selected = if (selector.isEmpty()) elements else doc.select(selector)
            return CheerioObject(doc, selected)
        }
        
        fun find(selector: String): CheerioObject {
            val found = Elements()
            elements.forEach { el -> found.addAll(el.select(selector)) }
            return CheerioObject(doc, found)
        }
        
        fun text(): String = elements.text()
        fun html(): String = elements.firstOrNull()?.html() ?: ""
        fun attr(name: String): String = elements.firstOrNull()?.attr(name) ?: ""
        
        fun first(): CheerioObject {
            val first = elements.firstOrNull()
            return CheerioObject(doc, if (first != null) Elements(first) else Elements())
        }
        
        fun last(): CheerioObject {
            val last = elements.lastOrNull()
            return CheerioObject(doc, if (last != null) Elements(last) else Elements())
        }
        
        fun eq(index: Int): CheerioObject {
            val el = elements.getOrNull(index)
            return CheerioObject(doc, if (el != null) Elements(el) else Elements())
        }
        
        fun map(callback: Any?): MapResult {
            val results = elements.map { el -> CheerioObject(doc, Elements(el)) }
            return MapResult(results)
        }
        
        fun toArray(): List<String> = elements.map { it.text() }
        fun get(): List<String> = elements.map { it.text() }
        fun get(index: Int): CheerioObject? {
            val el = elements.getOrNull(index)
            return if (el != null) CheerioObject(doc, Elements(el)) else null
        }
        
        fun each(callback: Any?): CheerioObject = this
        fun remove(): CheerioObject { elements.forEach { it.remove() }; return this }
        fun addBack(): CheerioObject = this
        fun contents(): CheerioObject = this
        fun filter(callback: Any?): CheerioObject = this
        
        fun next(): CheerioObject {
            val nextElements = Elements()
            elements.forEach { el -> el.nextElementSibling()?.let { nextElements.add(it) } }
            return CheerioObject(doc, nextElements)
        }
        
        fun prev(): CheerioObject {
            val prevElements = Elements()
            elements.forEach { el -> el.previousElementSibling()?.let { prevElements.add(it) } }
            return CheerioObject(doc, prevElements)
        }
        
        fun parent(): CheerioObject {
            val parents = Elements()
            elements.forEach { el -> el.parent()?.let { if (it is Element) parents.add(it) } }
            return CheerioObject(doc, parents)
        }
        
        fun children(): CheerioObject {
            val children = Elements()
            elements.forEach { el -> children.addAll(el.children()) }
            return CheerioObject(doc, children)
        }
        
        fun siblings(): CheerioObject {
            val siblings = Elements()
            elements.forEach { el -> siblings.addAll(el.siblingElements()) }
            return CheerioObject(doc, siblings)
        }
        
        fun hasClass(className: String): Boolean = elements.any { it.hasClass(className) }
        fun addClass(className: String): CheerioObject { elements.forEach { it.addClass(className) }; return this }
        fun removeClass(className: String): CheerioObject { elements.forEach { it.removeClass(className) }; return this }
        fun toggleClass(className: String): CheerioObject { elements.forEach { it.toggleClass(className) }; return this }
        fun isEmpty(): Boolean = elements.isEmpty()
        
        fun append(content: Any?): CheerioObject {
            val contentStr = content?.toString() ?: ""
            elements.forEach { el -> el.append(contentStr) }
            return this
        }
        
        fun prepend(content: Any?): CheerioObject {
            val contentStr = content?.toString() ?: ""
            elements.forEach { el -> el.prepend(contentStr) }
            return this
        }
        
        fun replaceWith(content: Any?): CheerioObject {
            val contentStr = content?.toString() ?: ""
            elements.forEach { el ->
                val parsed = Ksoup.parse(contentStr).body()
                parsed.children().forEach { child -> el.before(child) }
                el.remove()
            }
            return this
        }
        
        fun before(content: Any?): CheerioObject {
            val contentStr = content?.toString() ?: ""
            elements.forEach { el -> el.before(contentStr) }
            return this
        }
        
        fun after(content: Any?): CheerioObject {
            val contentStr = content?.toString() ?: ""
            elements.forEach { el -> el.after(contentStr) }
            return this
        }
        
        fun empty(): CheerioObject { elements.forEach { el -> el.empty() }; return this }
        
        fun clone(): CheerioObject {
            val cloned = Elements()
            elements.forEach { el -> cloned.add(el.clone()) }
            return CheerioObject(doc, cloned)
        }
        
        val length: Int get() = elements.size
    }
    
    class MapResult(private val results: List<Any?>) {
        fun get(): List<Any?> = results
        fun toArray(): List<Any?> = results
    }
}
