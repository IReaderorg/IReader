package ireader.presentation.di

import ireader.ui.appearance.di.AppearanceModules
import ireader.ui.book.di.BookModules
import ireader.ui.chapter.di.ChapterModules
import ireader.ui.downloader.di.DownloaderModules
import ireader.ui.explore.di.ExploreModules
import ireader.ui.history.di.HistoryModules
import ireader.ui.imageloader.di.ImageLoaderModules
import ireader.ui.library.di.LibraryModules
import ireader.ui.reader.di.ReaderModules
import ireader.ui.settings.di.SettingsModules
import ireader.ui.sources.di.SourcesModules
import ireader.ui.tts.di.TTSModules
import ireader.ui.updates.di.UpdatesModules
import ireader.ui.web.di.WebModules
import org.koin.ksp.generated.*

val uiModules = listOf(
    PresentationModules().module,
    WebModules().module,
    UpdatesModules().module,
    TTSModules().module,
    SourcesModules().module,
    SettingsModules().module,
    ReaderModules().module,
    LibraryModules().module,
    HistoryModules().module,
    ExploreModules().module,
    DownloaderModules().module,
    ChapterModules().module,
    BookModules().module,
    AppearanceModules().module,
)