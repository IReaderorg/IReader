package ireader.ui.library.di

import ireader.common.resources.ModulesMetaData
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan(ModulesMetaData.LIBRARY)
class LibraryModules