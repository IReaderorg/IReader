package ir.kazemcodes.infinity.explore_feature.domain.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.Constants.TEMP_BOOK

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = TEMP_BOOK)