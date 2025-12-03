package ireader.presentation.core.theme


expect class LocaleHelper {
    val languages :  MutableList<String>

    fun setLocaleLang()


    fun updateLocal()

    fun resetLocale()


    fun getLocales()


}
