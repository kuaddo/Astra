package jp.shiita.astra

import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import jp.shiita.astra.data.PreferenceStorage
import jp.shiita.astra.di.DaggerAppComponent
import timber.log.Timber
import javax.inject.Inject

class AstraApp : DaggerApplication() {
    @Inject
    lateinit var preferenceStorage: PreferenceStorage

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
            .applicationContext(this.applicationContext)
            .build()
    }
}