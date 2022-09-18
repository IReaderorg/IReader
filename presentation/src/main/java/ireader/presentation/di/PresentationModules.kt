package ireader.presentation.di


import ireader.i18n.ModulesMetaData
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan(ModulesMetaData.PRESENTATION)
class PresentationModules