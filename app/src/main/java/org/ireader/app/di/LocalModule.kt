package org.ireader.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.ireader.core_api.prefs.AndroidPreferenceStore
import org.ireader.core_api.prefs.PreferenceStore
import org.ireader.image_loader.LibraryCovers
import java.io.File
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class LocalModule {
    @Provides
    @Singleton
    fun provideLibraryCovers(
        @ApplicationContext context: Context,
    ): LibraryCovers {
        return org.ireader.image_loader.LibraryCovers(
            FileSystem.SYSTEM,
            File(context.filesDir, "library_covers").toOkioPath()
        )
    }


    @Provides
    @Singleton
    fun providePreferencesStore(@ApplicationContext context: Context): PreferenceStore {
        return AndroidPreferenceStore(context = context, "ui")
    }

}
