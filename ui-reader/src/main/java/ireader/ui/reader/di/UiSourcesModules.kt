package ireader.ui.reader.di

import ireader.common.resources.ModulesMetaData
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan(ModulesMetaData.READER)
class ReaderModules