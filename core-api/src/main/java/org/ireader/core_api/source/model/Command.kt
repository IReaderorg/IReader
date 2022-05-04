package org.ireader.core_api.source.model

sealed class Command<V>(val name: String, val initialValue: V) {

    object Detail {
        open class Fetch(url: String, html: String = "") : Command<String>(url,html)
    }
    object Content {
        open class Fetch(url: String, html: String = "") : Command<String>(url,html)
    }
    object Explore {
        open class Fetch(url: String, html: String = "") : Command<String>(url,html)
    }


    object Chapter {
        class Note(name: String) : Command<Unit>(name, Unit)
        open class Text(name: String, value: String = "") : Command<String>(name, value)
        open class Numeric(name: String, value: Int = -1) : Command<Int>(name, value)
        open class Range(name: String, firstValue: Int = -1,secondValue:Int = -1) : Command<Pair<Int,Int>>(name, Pair(firstValue,secondValue))
        open class Select(
            name: String,
            val options: Array<String>,
            value: Int = 0
        ) : Command<Int>(name, value)
        open class Fetch(url: String, html: String = "") : Command<String>(url,html)
    }
}