package jp.shiita.astra.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import jp.shiita.astra.R
import jp.shiita.astra.receiver.HangUpReceiver
import jp.shiita.astra.ui.MainActivity
import jp.shiita.astra.ui.call.CallViewModel.Companion.MAX_REMAINING_TIME
import javax.inject.Inject

class AstraNotificationManager @Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManager?
) {
    private var builder: NotificationCompat.Builder? = null

    fun createInTalkNotification(remainingTime: Int) {
        notificationManager ?: return

        if (builder == null) {
            createChannel(
                PUSH_CHANNEL_ID,
                context.getString(R.string.notification_manager_channel_name),
                context.getString(R.string.notification_manager_channel_description),
                notificationManager
            )

            val intent = PendingIntent.getActivity(
                context,
                PUSH_REQUEST_CODE,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val title = context.getString(R.string.notification_in_talk_title)
            val color = ResourcesCompat.getColor(
                context.resources,
                R.color.primary,
                null
            )
            builder = NotificationCompat.Builder(context, PUSH_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_phone_in_talk)
                .setColor(color)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(intent)
                .addAction(createHangUpAction())
        }
        builder?.setContentText(context.getString(R.string.call_remaining_time, remainingTime))
            ?.setProgress(MAX_REMAINING_TIME, MAX_REMAINING_TIME - remainingTime, false)

        val notification = builder?.build() ?: return
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelInTalkNotification() {
        builder = null
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    private fun createChannel(
        id: String,
        name: String,
        description: String,
        manager: NotificationManager
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (id in manager.notificationChannels.map(NotificationChannel::getId)) return

            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
            channel.description = description
            manager.createNotificationChannel(channel)
        }
    }

    private fun createHangUpAction(): NotificationCompat.Action {
        val intent = PendingIntent.getBroadcast(
            context,
            HANG_UP_REQUEST_CODE,
            Intent(context, HangUpReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_hang_up,
            context.getString(R.string.notification_hang_up_text),
            intent
        ).build()
    }

    companion object {
        const val PUSH_CHANNEL_ID = "pushChannelId"
        const val NOTIFICATION_ID = 1234
        const val PUSH_REQUEST_CODE = 1000
        const val HANG_UP_REQUEST_CODE = 1001
    }
}