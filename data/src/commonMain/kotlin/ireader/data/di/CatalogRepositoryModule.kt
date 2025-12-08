package ireader.data.di

import ireader.data.catalog.CatalogRemoteRepositoryImpl
import ireader.data.services.SourceHealthCheckerImpl
import ireader.data.sourcecomparison.SourceComparisonRepositoryImpl
import ireader.data.sourcereport.SourceReportRepositoryImpl
import ireader.data.repository.SourceCredentialsRepositoryImpl
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.data.repository.SourceComparisonRepository
import ireader.domain.data.repository.SourceCredentialsRepository
import ireader.domain.data.repository.SourceReportRepository
import ireader.domain.services.SourceHealthChecker
import org.koin.dsl.module

/**
 * DI module for catalog/source-related repositories.
 * Contains CatalogRemoteRepository, SourceComparisonRepository, SourceCredentialsRepository,
 * SourceReportRepository, and SourceHealthChecker.
 */
val catalogRepositoryModule = module {
    single<CatalogRemoteRepository> { CatalogRemoteRepositoryImpl(get()) }
    single<SourceComparisonRepository> { SourceComparisonRepositoryImpl(get()) }
    single<SourceCredentialsRepository> { SourceCredentialsRepositoryImpl(get()) }
    single<SourceReportRepository> { SourceReportRepositoryImpl(get()) }
    single<SourceHealthChecker> { SourceHealthCheckerImpl(get()) }
}
