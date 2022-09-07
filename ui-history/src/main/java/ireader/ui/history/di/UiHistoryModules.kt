package ireader.ui.history.di

import ireader.common.resources.ModulesMetaData
import ireader.ui.history.viewmodel.HistoryState
import ireader.ui.history.viewmodel.HistoryStateImpl
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.dsl.module


@Module
@ComponentScan(ModulesMetaData.HISTORY)
class HistoryModules

val historyModule = module {
    factory<HistoryState> {
        HistoryStateImpl()
    }
}