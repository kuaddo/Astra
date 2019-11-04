package jp.shiita.astra.di.module

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import dagger.Module
import dagger.Provides
import jp.shiita.astra.util.AstraNotificationManager
import jp.shiita.astra.util.SkyWayManager
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    @JvmStatic
    fun provideNotificationManager(context: Context): NotificationManager? =
        context.getSystemService()

    @Provides
    @Singleton
    fun provideSkyWayManager(
        context: Context,
        notificationManager: AstraNotificationManager
    ): SkyWayManager = SkyWayManager(context, notificationManager)
}