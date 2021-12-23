package ir.kazemcodes.infinity

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.zhuinden.simplestack.GlobalServices
import com.zhuinden.simplestackextensions.servicesktx.add
import dagger.hilt.android.HiltAndroidApp
import ir.kazemcodes.infinity.data.repository.dataStore
import ir.kazemcodes.infinity.domain.use_cases.datastore.DataStoreUseCase
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.presentation.core.Constants.DatastoreServiceTAG
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class MyApplication : Application() {
    @Inject lateinit var localUseCase: LocalUseCase
    @Inject lateinit var remoteUseCase: RemoteUseCase
    @Inject lateinit var dataStoreUseCase: DataStoreUseCase

    lateinit var globalServices: GlobalServices
        private set
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        globalServices = GlobalServices.builder()
            .add(localUseCase)
            .add(remoteUseCase)
            .add(this.dataStore,DatastoreServiceTAG)
            .add(dataStoreUseCase)
            .build()
    }
}