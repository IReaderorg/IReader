package ireader.ui.web.di

import ireader.common.resources.ModulesMetaData
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.*

@Module
@ComponentScan(ModulesMetaData.WEB)
class WebModules

