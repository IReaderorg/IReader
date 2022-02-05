package ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences

import ir.kazemcodes.infinity.core.domain.repository.Repository
import ir.kazemcodes.infinity.feature_library.presentation.components.FilterType
import ir.kazemcodes.infinity.feature_library.presentation.components.SortType

class SaveOrientationUseCase(
    private val repository: Repository,
) {
    operator fun invoke(paragraphDistance: Int) {
        repository.preferencesHelper.orientation.set(paragraphDistance)
    }
}

class ReadOrientationUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Int {
        return repository.preferencesHelper.orientation.get()
    }
}
class SaveFiltersUseCase(
    private val repository: Repository,
) {
    operator fun invoke(value: Int) {
        repository.preferencesHelper.filterLibraryScreen.set(value)
    }
}

class ReadFilterUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): FilterType {
        return mapFilterType(repository.preferencesHelper.filterLibraryScreen.get())
    }
}

class SaveSortersUseCase(
    private val repository: Repository,
) {
    operator fun invoke(value: Int) {
        repository.preferencesHelper.sortLibraryScreen.set(value)
    }
}

class ReadSortersUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): SortType {
        return mapSortType(repository.preferencesHelper.sortLibraryScreen.get())
    }
}

fun mapSortType(input : Int)  : SortType{
    return when(input) {
        0 -> {
            SortType.DateAdded
        }
        1 -> {
            SortType.Alphabetically
        }
        2 -> {
            SortType.LastRead
        }
        else -> {
            SortType.TotalChapter
        }
    }
}

fun mapFilterType(input : Int)  : FilterType{
    return when(input) {
        0 -> {
            FilterType.Disable
        }
        else -> {
            FilterType.Unread
        }
    }
}


