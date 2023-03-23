package ireader.domain.utils

inline fun <T, R : Any> Collection<T>.mapNotNull(transform: (T) -> R?): List<R> {
    return mapNotNullTo(ArrayList<R>(), transform)
}