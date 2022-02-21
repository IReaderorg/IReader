package org.ireader.extensions.sources.en.webnovel

fun <T> merge(first: List<T>, second: List<T>): List<T> {
    return object : ArrayList<T>() {
        init {
            addAll(first)
            addAll(second)
        }
    }
}
