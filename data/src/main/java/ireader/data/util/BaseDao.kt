package ireader.data.util

abstract class BaseDao<T> {

    fun dbOperation(list: List<T>, onInsert : (T) -> Unit) {
        for (item in list) {
            onInsert(item)
        }
    }
}