package ireader.domain.utils.extensions.async


fun <E> List<E>.nextAfter(index: Int): E? = if (index == count() - 1) null else this[index + 1]

fun <E> List<E>.prevBefore(index: Int): E? = if (index == 0) null else this[index - 1]
