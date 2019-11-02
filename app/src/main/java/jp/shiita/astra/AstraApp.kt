package jp.shiita.astra

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import jp.shiita.astra.data.PreferenceStorage
import timber.log.Timber
import javax.inject.Inject

class AstraApp : Application() {
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
}