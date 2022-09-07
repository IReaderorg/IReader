package ireader.presentation.di


import ireader.common.resources.ModulesMetaData
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named

@Module
@ComponentScan(ModulesMetaData.PRESENTATION)
class PresentationModules