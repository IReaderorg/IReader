package ireader.ui.home.sources.extension

import androidx.compose.ui.text.intl.LocaleList
import ireader.common.models.entities.CatalogLocal

class UserLanguagesComparator : Comparator<Language> {

    private val userLanguages = mutableMapOf<String, Int>()

    init {
        val userLocales = LocaleList.current.localeList
        val size = userLocales.size
        for (locale in userLocales) {
            userLanguages[locale.language] = size - userLanguages.size
        }
    }

    override fun compare(a: Language, b: Language): Int {
        val langOnePosition = userLanguages[a.code] ?: 0
        val langTwoPosition = userLanguages[b.code] ?: 0

        return langTwoPosition.compareTo(langOnePosition)
    }
}

class InstalledLanguagesComparator(
    localCatalogs: List<CatalogLocal>,
) : Comparator<Language> {

    private val preferredLanguages = localCatalogs
        .groupBy { it.source?.lang }
        .mapValues { it.value.size }

    override fun compare(a: Language, b: Language): Int {
        val langOnePosition = preferredLanguages[a.code] ?: 0
        val langTwoPosition = preferredLanguages[b.code] ?: 0

        return langTwoPosition.compareTo(langOnePosition)
    }
}
