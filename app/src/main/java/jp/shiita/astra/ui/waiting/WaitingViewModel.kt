package jp.shiita.astra.ui.waiting

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import jp.shiita.astra.extensions.combineLatest
import jp.shiita.astra.model.celestialsphere.CelestialSphere
import jp.shiita.astra.util.live.LocationLiveData
import jp.shiita.astra.util.live.OrientationLiveData
import jp.shiita.astra.util.live.UnitLiveEvent
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class WaitingViewModel @Inject constructor(
    locationLiveData: LocationLiveData,
    orientationLiveData: OrientationLiveData
) : ViewModel() {
    // とりあえずDIとかは考えない
    private val celestialSphere = CelestialSphere()

    val celestialGrid: LiveData<CelestialSphere.CelestialGrid> =
        locationLiveData.combineLatest(orientationLiveData) { location, deviceOrientation ->
            celestialSphere.searchGrid(location, deviceOrientation)
        }
    val debugPhi: LiveData<Int> = celestialGrid.map { it.phiGridNum }
    val debugTheta: LiveData<Int> = celestialGrid.map { it.thetaGridNum }

    val startCallingEvent: LiveData<Unit>
        get() = _startCallingEvent

    private var _startCallingEvent = UnitLiveEvent()

    private val celestialGridObserver: (CelestialSphere.CelestialGrid) -> Unit =
        { Timber.d(it.toString()) }

    override fun onCleared() {
        super.onCleared()
        celestialGrid.removeObserver(celestialGridObserver)
    }

    // TODO: 一時的な処理
    fun start() = viewModelScope.launch {
        //        delay(2000)
//        _startCallingEvent.call()
    }

    fun startGridObserve() = celestialGrid.observeForever(celestialGridObserver)
}