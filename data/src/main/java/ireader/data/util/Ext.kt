package ireader.data.util

fun Boolean.toLong() : Long{
 return   if (this) 1 else 0
}
fun Long.toDB() : Long?{
 return if (this == 0L) null else this
}