package ireader.presentation.core.theme

import ireader.core.prefs.Preference


expect class LocaleHelper {
    val languages :  MutableList<String>

    fun setLocaleLang()


    fun updateLocal()

    fun resetLocale()


    fun getLocales()


}