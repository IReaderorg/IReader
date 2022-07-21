package org.ireader.common_data

interface TranslateEngine {

    val id:Long

    suspend fun translate(texts:List<String>,source:String,target: String,onSuccess: (List<String>) -> Unit)

}