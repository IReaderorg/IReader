package ireader.presentation.ui.home.sources.extension

import androidx.compose.ui.text.intl.LocaleList
import ireader.domain.models.entities.CatalogLocal

class UserLanguagesComparator : Comparator<Language> {

    private val userLanguages: Map<String, Int> = run {
        val userLocales = LocaleList.current.localeList
        val size = userLocales.size
        val map = mutableMapOf<String, Int>()
        for (locale in userLocales) {
            map[locale.language] = size - map.size
        }
        map
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
