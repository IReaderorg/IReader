package ir.kazemcodes.infinity.core.utils

fun <T> merge(first: List<T>, second: List<T>): List<T> {
    return object : ArrayList<T>() {
        init {
            addAll(first)
            addAll(second)
        }
    }
}

fun Any.onCondition(condition: Boolean?,doThis: (item:Any) -> Unit) : Any {
    if (condition == true) {

        return doThis(this)
    } else {
        return this
    }
}