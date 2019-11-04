package jp.shiita.astra.receiver

import android.content.Context
import android.content.Intent
import dagger.android.DaggerBroadcastReceiver
import jp.shiita.astra.util.SkyWayManager
import javax.inject.Inject

class HangUpReceiver : DaggerBroadcastReceiver() {
    @Inject
    lateinit var skyWayManager: SkyWayManager

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        skyWayManager.closeConnection()
    }
}