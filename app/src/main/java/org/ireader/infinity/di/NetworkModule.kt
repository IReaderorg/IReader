package org.ireader.infinity.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CookieJar
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.data.repository.NetworkPreferences
import org.ireader.data.repository.RemoteKeyRepositoryImpl
import org.ireader.domain.repository.RemoteKeyRepository
import org.ireader.domain.utils.MemoryCookieJar
import tachiyomi.core.prefs.PreferenceStore
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {


    @Provides
    @Singleton
    fun providesCookieJar(): CookieJar {
        return MemoryCookieJar()
    }


    @Provides
    @Singleton
    fun provideRemoteKeyRepository(
        remoteKeysDao: RemoteKeysDao,
    ): RemoteKeyRepository {
        return RemoteKeyRepositoryImpl(
            dao = remoteKeysDao
        )
    }




    @Singleton
    @Provides
    fun provideNetworkPreference(preferenceStore: PreferenceStore): NetworkPreferences {
        return NetworkPreferences(preferenceStore)
    }




}
