package ireader.domain.di

import android.app.Service
import ireader.domain.services.downloaderService.DownloaderService
import ireader.domain.services.library_update_service.LibraryUpdatesService
import ireader.domain.services.tts_service.media_player.TTSService
import ireader.domain.services.update_service.UpdateService
import ireader.i18n.ModulesMetaData
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module

@Module
@ComponentScan(ModulesMetaData.DOMAIN)
class DomainModules


val DomainServices = module {
    worker {
        DownloaderService(get(),get(),get(),get(),get(),get(),get(),get(),get(),get())
    }
    worker {
        UpdateService(get(), get(),get(),get())
    }
    worker {
        LibraryUpdatesService(get(), get(),get(),get(),get(),get(),get(),get(),get())
    }
    single<Service> (createdAtStart=true){
        TTSService()
    }
}