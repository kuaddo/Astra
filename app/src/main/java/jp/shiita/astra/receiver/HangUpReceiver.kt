package jp.shiita.astra.receiver

import android.content.Context
import android.content.Intent
import dagger.android.DaggerBroadcastReceiver
import jp.shiita.astra.AstraApp
import javax.inject.Inject

class HangUpReceiver : DaggerBroadcastReceiver() {
    @Inject
    lateinit var application: AstraApp

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        application.closeAllSkyWayManager()
    }
}