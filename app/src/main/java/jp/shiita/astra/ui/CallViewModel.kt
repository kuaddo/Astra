package jp.shiita.astra.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import jp.shiita.astra.extensions.combineLatest
import jp.shiita.astra.model.ErrorResource
import jp.shiita.astra.model.SuccessResource
import jp.shiita.astra.model.celestialsphere.CelestialSphere
import jp.shiita.astra.repository.AstraRepository
import jp.shiita.astra.util.SkyWayManager
import jp.shiita.astra.util.live.LiveEvent
import jp.shiita.astra.util.live.LocationLiveData
import jp.shiita.astra.util.live.OrientationLiveData
import jp.shiita.astra.util.live.UnitLiveEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CallViewModel @Inject constructor(
    private val skyWayManager: SkyWayManager,
    private val repository: AstraRepository,
    locationLiveData: LocationLiveData,
    orientationLiveData: OrientationLiveData
) : ViewModel() {

    // とりあえずDIとかは考えない
    private val celestialSphere = CelestialSphere()
    private val celestialGrid: LiveData<CelestialSphere.CelestialGrid> =
        locationLiveData.combineLatest(orientationLiveData) { location, deviceOrientation ->
            val grid = celestialSphere.searchGrid(location, deviceOrientation)
            Timber.d("phi = ${grid.phiGridNum}, theta = ${grid.thetaGridNum}")
            grid
        }

    val remainingTimeSecond: LiveData<Int>
        get() = _remainingTimeSecond
    val connected: LiveData<Boolean>
        get() = skyWayManager.connected
    val isOwnIdAvailable: LiveData<Boolean>
        get() = skyWayManager.ownId.map { it.isNotBlank() }
    val onStopConnectionEvent: LiveData<Unit>
        get() = skyWayManager.onStopConnectionEvent

    val startCallingEvent: LiveData<Unit>
        get() = _startCallingEvent
    val selectUploadImageEvent: LiveData<String>
        get() = _selectUploadImageEvent
    val viewImageEvent: LiveData<String>
        get() = _viewImageEvent

    val progress: Float
        get() {
            val progressTime = _remainingTimeSecond.value ?: 0
            return (MAX_REMAINING_TIME - progressTime) / MAX_REMAINING_TIME.toFloat()
        }

    private val _remainingTimeSecond = MutableLiveData(MAX_REMAINING_TIME)

    private val _startCallingEvent = UnitLiveEvent()
    private val _selectUploadImageEvent = LiveEvent<String>()
    private val _viewImageEvent = LiveEvent<String>()

    private var isLoading = false
    private var isMatched = false   // TODO: できれば使いたくない。removeObserverが意図したように動かないので仕方なく使う
    private var beforeGrid: CelestialSphere.CelestialGrid? = null

    private val startConnectionObserver: (Unit) -> Unit =
        { finishMatching() }
    private val celestialGridObserver: (CelestialSphere.CelestialGrid) -> Unit =
        { startMatching(it) }

    init {
        skyWayManager.onStartConnectionEvent.observeForever(startConnectionObserver)
    }

    override fun onCleared() {
        deleteSkyWayId()    // おそらく意味がない状態になっている
        skyWayManager.destroy()
    }

    fun startGridObserve() = celestialGrid.observeForever(celestialGridObserver)

    fun stopGridObserve() = celestialGrid.removeObserver(celestialGridObserver)

    fun hangUp() = skyWayManager.closeConnection()

    fun startLocalStream() = skyWayManager.startLocalStream()

    fun startCountDown() = viewModelScope.launch {
        if (_remainingTimeSecond.value != MAX_REMAINING_TIME) return@launch

        while (_remainingTimeSecond.value ?: 0 > 0 && connected.value == true) {
            val time = _remainingTimeSecond.value!! - 1
            _remainingTimeSecond.value = time
            skyWayManager.updateRemainingTime(time)
            Timber.d("RemainingTime = $time")

            delay(1000)
        }
        if (connected.value == true) hangUp()
    }

    fun selectUploadImage() {
        _selectUploadImageEvent.value = skyWayManager.imageShareId.value
    }

    fun viewImage() {
        _viewImageEvent.value = skyWayManager.imageShareId.value
    }

    private fun openConnection(opponentPeerId: String) =
        skyWayManager.openConnection(opponentPeerId)

    private fun startMatching(grid: CelestialSphere.CelestialGrid) = viewModelScope.launch {
        if (isLoading) return@launch
        if (isMatched) return@launch
        if (grid == beforeGrid) return@launch
        if (!skyWayManager.isStartedLocalStream) return@launch

        isLoading = true
        skyWayManager.ownId.value?.let { id ->
            when (val res = repository.postSkyWayId(id, grid.phiGridNum, grid.thetaGridNum)) {
                is SuccessResource -> {
                    beforeGrid = grid
                    if (res.data != null) openConnection(res.data)
                }
                is ErrorResource -> {
                    Timber.d("post error SkyWayID = $id, phi = ${grid.phiGridNum}, theta = ${grid.thetaGridNum}")
                }
            }
            isLoading = false
        }
    }

    private fun finishMatching() {
        isMatched = true
        stopGridObserve()
        deleteSkyWayId()
        skyWayManager.onStartConnectionEvent.removeObserver(startConnectionObserver)
        _startCallingEvent.call()
    }

    private fun deleteSkyWayId() = viewModelScope.launch {
        skyWayManager.ownId.value?.let { id ->
            when (repository.deleteSkyWayId(id)) {
                is SuccessResource -> Timber.d("delete SkyWayID : $id")
                is ErrorResource -> Timber.d("delete SkyWayID is failed : $id")
            }
        }
    }

    companion object {
        const val MAX_REMAINING_TIME = 180
    }
}