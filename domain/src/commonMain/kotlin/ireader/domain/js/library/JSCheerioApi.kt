package ireader.domain.js.library

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * Native Cheerio API implementation using jsoup.
 * Provides a cheerio-compatible interface for HTML parsing.
 */
class JSCheerioApi(private val pluginId: String) {
    
    /**
     * Loads HTML and returns a cheerio-like object.
     * Returns a CheerioObject that can be used as both a function and an object.
     */
    fun load(html: String): CheerioObject {
        println("[JSCheerioApi] [$pluginId] Loading HTML: ${html.length} characters")
        val doc = Jsoup.parse(html)
        println("[JSCheerioApi] [$pluginId] Parsed document, total elements: ${doc.select("*").size}")
        
        // Debug: check if reader-area exists
        val readerArea = doc.select("#reader-area")
        println("[JSCheerioApi] [$pluginId] Elements with id='reader-area': ${readerArea.size}")
        if (readerArea.isEmpty()) {
            // Try to find what IDs exist
            val allIds = doc.select("[id]").map { it.id() }.take(10)
            println("[JSCheerioApi] [$pluginId] Sample IDs in document: $allIds")
        }
        
        return CheerioObject(doc, doc.select("*"))
    }
    

    /**
     * Cheerio object representing selected elements.
     * Can be called as a function to select elements.
     */
    class CheerioObject(
        private val doc: Document,
        private val elements: Elements
    ) {
        
        /**
         * Allows calling the object as a selector function.
         * This is used when plugins do: const $ = cheerio.load(html); $('selector')
         */
        operator fun invoke(selector: String): CheerioObject {
            val selected = if (selector.isEmpty()) {
                elements
            } else {
                // Try the selector as-is first
                var result = doc.select(selector)
                
                // If no results and it's an ID selector, try matching IDs that start with it
                if (result.isEmpty() && selector.startsWith("#")) {
                    val idPrefix = selector.substring(1)
                    result = doc.select("[id^=$idPrefix]")
                    println("[JSCheerioApi] ID selector '$selector' not found, trying prefix match: found ${result.size} elements")
                }
                
                result
            }
            println("[JSCheerioApi] invoke('$selector') returned ${selected.size} elements")
            return CheerioObject(doc, selected)
        }
        
        fun find(selector: String): CheerioObject {
            val found = Elements()
            elements.forEach { el ->
                found.addAll(el.select(selector))
            }
            println("[JSCheerioApi] find('$selector') returned ${found.size} elements")
            return CheerioObject(doc, found)
        }
        
        fun text(): String {
            return elements.text()
        }
        
        fun html(): String {
            // Return inner HTML of first element, or empty string if no elements
            val result = elements.firstOrNull()?.html() ?: ""
            println("[JSCheerioApi] html() returned ${result.length} characters from ${elements.size} elements")
            return result
        }
        
        fun attr(name: String): String {
            // Return attribute value or empty string if not found
            return elements.firstOrNull()?.attr(name) ?: ""
        }
        
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
        
        /**
         * Map over elements. The callback receives (index, element) where element is a CheerioObject.
         * This matches Cheerio's behavior where the callback gets a wrapped element.
         */
        fun map(callback: Any?): MapResult {
            // Convert elements to CheerioObjects for the callback
            val results = mutableListOf<Any?>()
            elements.forEachIndexed { index, el ->
                // Wrap each element in a CheerioObject
                val wrappedEl = CheerioObject(doc, Elements(el))
                // The callback will be called from JavaScript, so we just collect the wrapped elements
                // The actual callback execution happens in JavaScript
                results.add(wrappedEl)
            }
            return MapResult(results)
        }
        
        fun toArray(): List<String> {
            // Return text content of elements as strings
            return elements.map { it.text() }
        }
        
        fun get(): List<String> {
            // Return text content of elements as strings
            return elements.map { it.text() }
        }
        
        fun get(index: Int): CheerioObject? {
            // Return a CheerioObject wrapping the element
            val el = elements.getOrNull(index)
            return if (el != null) CheerioObject(doc, Elements(el)) else null
        }
        
        /**
         * Iterate over elements with a callback.
         * The callback receives (index, element) where element is a CheerioObject.
         * This is a stub that returns the CheerioObject for JavaScript to iterate over.
         */
        fun each(callback: Any?): CheerioObject {
            // The actual iteration happens in JavaScript
            // We just return this to allow chaining
            return this
        }
        
        /**
         * Remove elements from the selection.
         */
        fun remove(): CheerioObject {
            elements.forEach { it.remove() }
            return this
        }
        
        /**
         * Add back previously selected elements.
         */
        fun addBack(): CheerioObject {
            // Return this for now (simplified implementation)
            return this
        }
        
        /**
         * Get contents including text nodes.
         */
        fun contents(): CheerioObject {
            // Return this for now (simplified implementation)
            return this
        }
        
        /**
         * Filter elements.
         */
        fun filter(callback: Any?): CheerioObject {
            // Return this for now (simplified implementation)
            return this
        }
        
        /**
         * Get the next sibling element.
         */
        fun next(): CheerioObject {
            val nextElements = Elements()
            elements.forEach { el ->
                el.nextElementSibling()?.let { nextElements.add(it) }
            }
            return CheerioObject(doc, nextElements)
        }
        
        /**
         * Get the previous sibling element.
         */
        fun prev(): CheerioObject {
            val prevElements = Elements()
            elements.forEach { el ->
                el.previousElementSibling()?.let { prevElements.add(it) }
            }
            return CheerioObject(doc, prevElements)
        }
        
        /**
         * Get parent elements.
         */
        fun parent(): CheerioObject {
            val parents = Elements()
            elements.forEach { el ->
                el.parent()?.let { parents.add(it) }
            }
            return CheerioObject(doc, parents)
        }
        
        /**
         * Get children elements.
         */
        fun children(): CheerioObject {
            val children = Elements()
            elements.forEach { el ->
                children.addAll(el.children())
            }
            return CheerioObject(doc, children)
        }
        
        /**
         * Get sibling elements.
         */
        fun siblings(): CheerioObject {
            val siblings = Elements()
            elements.forEach { el ->
                siblings.addAll(el.siblingElements())
            }
            return CheerioObject(doc, siblings)
        }
        
        /**
         * Check if elements have a class.
         */
        fun hasClass(className: String): Boolean {
            return elements.any { it.hasClass(className) }
        }
        
        /**
         * Add a class to elements.
         */
        fun addClass(className: String): CheerioObject {
            elements.forEach { it.addClass(className) }
            return this
        }
        
        /**
         * Remove a class from elements.
         */
        fun removeClass(className: String): CheerioObject {
            elements.forEach { it.removeClass(className) }
            return this
        }
        
        /**
         * Toggle a class on elements.
         */
        fun toggleClass(className: String): CheerioObject {
            elements.forEach { it.toggleClass(className) }
            return this
        }
        
        /**
         * Check if selection is empty.
         */
        fun isEmpty(): Boolean {
            return elements.isEmpty()
        }
        
        /**
         * Append content to elements.
         */
        fun append(content: Any?): CheerioObject {
            val contentStr = content?.toString() ?: ""
            elements.forEach { el ->
                el.append(contentStr)
            }
            return this
        }
        
        /**
         * Prepend content to elements.
         */
        fun prepend(content: Any?): CheerioObject {
            val contentStr = content?.toString() ?: ""
            elements.forEach { el ->
                el.prepend(contentStr)
            }
            return this
        }
        
        /**
         * Replace elements with new content.
         */
        fun replaceWith(content: Any?): CheerioObject {
            val contentStr = content?.toString() ?: ""
            elements.forEach { el ->
                val parsed = org.jsoup.Jsoup.parse(contentStr).body()
                parsed.children().forEach { child ->
                    el.before(child)
                }
                el.remove()
            }
            return this
        }
        
        /**
         * Insert content before elements.
         */
        fun before(content: Any?): CheerioObject {
            val contentStr = content?.toString() ?: ""
            elements.forEach { el ->
                el.before(contentStr)
            }
            return this
        }
        
        /**
         * Insert content after elements.
         */
        fun after(content: Any?): CheerioObject {
            val contentStr = content?.toString() ?: ""
            elements.forEach { el ->
                el.after(contentStr)
            }
            return this
        }
        
        /**
         * Empty the elements (remove all children).
         */
        fun empty(): CheerioObject {
            elements.forEach { el ->
                el.empty()
            }
            return this
        }
        
        /**
         * Clone the elements.
         */
        fun clone(): CheerioObject {
            val cloned = Elements()
            elements.forEach { el ->
                cloned.add(el.clone())
            }
            return CheerioObject(doc, cloned)
        }
        
        val length: Int
            get() = elements.size
    }
    
    /**
     * Result of a map operation.
     */
    class MapResult(private val results: List<Any?>) {
        fun get(): List<Any?> = results
        fun toArray(): List<Any?> = results
    }
}
