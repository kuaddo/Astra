package jp.shiita.astra.util.live

import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import javax.inject.Inject

class LocationLiveData @Inject constructor(
    private var fusedLocationProviderClient: FusedLocationProviderClient?
) : LiveData<Location>() {

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            value = result.lastLocation
        }
    }

    override fun onActive() {
        super.onActive()
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000         // 5sec
            fastestInterval = 5000  // 5sec
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    override fun onInactive() {
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
    }
}