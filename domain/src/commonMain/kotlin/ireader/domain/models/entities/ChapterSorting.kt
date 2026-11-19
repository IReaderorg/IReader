package ireader.domain.models.entities

import ireader.core.source.model.ChapterInfo

/**
 * Comparators for chapter lists.
 *
 * Plain string/raw-number sorting puts `c10` before `c2` because most sources
 * leave [Chapter.number] at its `-1f` sentinel. These comparators understand
 * numeric parts of a chapter name so `c1, c2, ..., c10` sorts in reading order.
 */
object ChapterSorting {

    /** Source list order, with stable tiebreakers for chapters the source omitted. */
    val BySource: Comparator<Chapter> = Comparator { a, b ->
        compareValues(a.sourceOrder, b.sourceOrder)
            .then { compareValues(a.number, b.number) }
            .then { compareValues(a.id, b.id) }
    }

    val ByName: Comparator<Chapter> = Comparator { a, b ->
        naturalCompare(a.name, b.name)
            .then { compareValues(a.sourceOrder, b.sourceOrder) }
            .then { compareValues(a.id, b.id) }
    }

    val ByNumber: Comparator<Chapter> = Comparator { a, b ->
        compareValues(a.sortNumber, b.sortNumber)
            .then { naturalCompare(a.name, b.name) }
            .then { compareValues(a.id, b.id) }
    }
}

/**
 * True when stored chapters cannot express the website order: more than one
 * chapter shares a `sourceOrder`. Legacy downloaded chapters all default to 0,
 * so they fall back to insertion order and look different from the source list.
 */
fun List<Chapter>.hasStaleSourceOrder(): Boolean =
    size > 1 && map { it.sourceOrder }.toSet().size != size

/** Chapter number from the source, or one parsed from the name when absent. */
val Chapter.sortNumber: Float
    get() = if (isRecognizedNumber) {
        number
    } else {
        val extracted = ChapterInfo.extractChapterNumber(name)
        if (extracted >= 0f) {
            extracted
        } else {
            NUMBER_AT_END.find(name)?.value?.toFloatOrNull() ?: -1f
        }
    }

private val NUMBER_AT_END = Regex("""\d+(?:\.\d+)?""")

/** Case-insensitive comparison where runs of digits compare as numbers. */
fun naturalCompare(a: String, b: String): Int {
    val aTokens = a.toTokens()
    val bTokens = b.toTokens()
    var index = 0
    while (index < aTokens.size && index < bTokens.size) {
        val first = aTokens[index]
        val second = bTokens[index]
        val result = if (first[0].isDigit() && second[0].isDigit()) {
            compareNumericTokens(first, second)
        } else {
            val lowerFirst = first.lowercase()
            val lowerSecond = second.lowercase()
            val lowerResult = lowerFirst.compareTo(lowerSecond)
            if (lowerResult != 0) lowerResult else first.compareTo(second)
        }
        if (result != 0) return result
        index++
    }
    return aTokens.size.compareTo(bTokens.size)
}

private fun String.toTokens(): List<String> {
    if (isEmpty()) return emptyList()
    val tokens = mutableListOf<String>()
    val current = StringBuilder()
    var currentIsDigit: Boolean? = null
    for (char in this) {
        val isDigit = char.isDigit()
        if (currentIsDigit != null && isDigit != currentIsDigit) {
            tokens.add(current.toString())
            current.clear()
        }
        currentIsDigit = isDigit
        current.append(char)
    }
    tokens.add(current.toString())
    return tokens
}

private fun compareNumericTokens(a: String, b: String): Int {
    val trimmedA = a.trimStart('0').ifEmpty { "0" }
    val trimmedB = b.trimStart('0').ifEmpty { "0" }
    return trimmedA.length.compareTo(trimmedB.length)
        .takeIf { it != 0 } ?: trimmedA.compareTo(trimmedB)
}

private inline fun Int.then(next: () -> Int): Int = if (this != 0) this else next()
