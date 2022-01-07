package ir.kazemcodes.infinity.util

fun List<String>.formatBasedOnDot(): String {
    return this.joinToString { it.trim() }.replace(".", ".\n")
}

fun List<String>.formatList(): String {
    return this.map { it.trim() }.joinToString("-").replace("\"", "").replace("[", "").replace("]", "")
}
fun String.shouldSubstring(condition: Boolean?,string: String,unit : ((string: String) -> String)?=null) : String{
    return if (condition == true) {
        if (unit !=null) {
            return  string + unit(this)

        }else {
            return  string + this
        }
    }else {
        this
    }
}
fun String.replaceImageFormat(condition: Boolean) : String {
    return if (condition) {
        this.replace(".webp","")
    }else {
        this
    }
}

fun String.applyPageFormat(page: Int) : String {
    return this.replace("{page}",page.toString())
}
fun String.applySearchFormat(query : String,page: Int) : String {
    return this.replace("{query}",page.toString())
}
