package jp.shiita.astra.di.module

import android.app.NotificationManager
import android.content.Context
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides

@Module
object AppModule {

    @Provides
    @JvmStatic
    fun provideNotificationManager(context: Context): NotificationManager? =
        context.getSystemService()

    @Provides
    @JvmStatic
    fun provideSensorManager(context: Context): SensorManager? =
        context.getSystemService()

    @Provides
    @JvmStatic
    fun provideFusedLocationProviderClient(context: Context): FusedLocationProviderClient? =
        LocationServices.getFusedLocationProviderClient(context)
}