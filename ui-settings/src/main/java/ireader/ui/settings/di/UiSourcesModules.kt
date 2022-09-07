package ireader.ui.settings.di

import ireader.common.resources.ModulesMetaData
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan(ModulesMetaData.SETTINGS)
class SettingsModules