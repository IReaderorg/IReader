package org.ireader.core_api.source.model

sealed class Command<V>(val name: String, val initialValue: V) {

    /**
     * The value of this command, with the initial value set.
     */
    var value = initialValue

    /**
     * Whether this command has been updated. If this method returns true, the catalog won't receive
     * this command when performing an operation.
     */
    open fun isDefaultValue(): Boolean {
        return value == initialValue
    }

    object Detail {
        open class Fetch(val url: String = "",val html: String = "") : Command<String>(url, html)
    }

    object Content {
        open class Fetch(val url: String = "",val  html: String = "") : Command<String>(url, html)
    }

    object Explore {
        open class Fetch(val url: String = "",val  html: String = "") : Command<String>(url, html)
    }

    object Chapter {
        class Note(name: String) : Command<Unit>(name, Unit)
        open class Text(name: String, value: String = "") : Command<String>(name, value)
        open class Select(
            name: String,
            val options: Array<String>,
            value: Int = 0
        ) : Command<Int>(name, value)

        open class Fetch(val url: String = "", val html: String = "") :
            Command<String>(url, html)
    }
}