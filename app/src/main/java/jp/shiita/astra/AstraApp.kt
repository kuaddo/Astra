package jp.shiita.astra

import androidx.lifecycle.LiveData
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import jp.shiita.astra.data.PreferenceStorage
import jp.shiita.astra.di.DaggerAppComponent
import jp.shiita.astra.util.live.UnitLiveEvent
import timber.log.Timber
import javax.inject.Inject

class AstraApp : DaggerApplication() {
    @Inject
    lateinit var preferenceStorage: PreferenceStorage

    val closeSkyWayManagerEvent: LiveData<Unit>
        get() = _closeSkyWayManagerEvent

    private val _closeSkyWayManagerEvent = UnitLiveEvent(generateUUIDTag = true)

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        preferenceStorage.preferenceChangedEvent.call() // 呼んでおくことで、mapした直後に値を反映できる
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder()
            .application(this)
            .applicationContext(this.applicationContext)
            .build()
    }

    fun closeAllSkyWayManager() = _closeSkyWayManagerEvent.call()
}