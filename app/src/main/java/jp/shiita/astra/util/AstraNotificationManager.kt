package jp.shiita.astra.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import jp.shiita.astra.R
import jp.shiita.astra.ui.MainActivity
import javax.inject.Inject

class AstraNotificationManager @Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManager?
) {
    private val channelName =
        context.getString(R.string.notification_manager_channel_name)
    private val channelDescription =
        context.getString(R.string.notification_manager_channel_description)

    fun createInTalkNotification() {
        notificationManager ?: return

        createChannel(PUSH_CHANNEL_ID, channelName, channelDescription, notificationManager)

        val intent = PendingIntent.getActivity(
            context,
            PUSH_REQUEST_CODE,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = context.getString(R.string.notification_in_talk_title)
        val text = context.getString(R.string.call_rest_time)
        val notification = NotificationCompat.Builder(context, PUSH_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_phone_in_talk)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(intent)
            .build()
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelInTalkNotification() =
        notificationManager?.cancel(NOTIFICATION_ID)

    private fun createChannel(
        id: String,
        name: String,
        description: String,
        manager: NotificationManager
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (id in manager.notificationChannels.map(NotificationChannel::getId)) return

            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = description
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val PUSH_CHANNEL_ID = "pushChannelId"
        const val NOTIFICATION_ID = 1234
        const val PUSH_REQUEST_CODE = 1000
    }
}