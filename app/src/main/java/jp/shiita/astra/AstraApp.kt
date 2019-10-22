package jp.shiita.astra

import android.app.Application
import timber.log.Timber

class AstraApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}