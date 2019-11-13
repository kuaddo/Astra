package jp.shiita.astra.ui.call

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import jp.shiita.astra.util.SkyWayManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CallViewModel @Inject constructor(private val skyWayManager: SkyWayManager) : ViewModel() {

    val remainingTimeSecond: LiveData<Int>
        get() = _remainingTimeSecond
    val ownId: LiveData<String>
        get() = skyWayManager.ownId
    val allPeerIds: LiveData<List<String>>  // TODO: sample
        get() = skyWayManager.allPeerIds
    val connected: LiveData<Boolean>
        get() = skyWayManager.connected
    val isOwnIdAvailable: LiveData<Boolean>
        get() = skyWayManager.ownId.map { it.isNotBlank() }
    val onStopConnectionEvent: LiveData<Unit>
        get() = skyWayManager.onStopConnectionEvent
    val onStartConnectionEvent: LiveData<Unit>
        get() = skyWayManager.onStartConnectionEvent

    private val _remainingTimeSecond = MutableLiveData<Int>()

    override fun onCleared() {
        skyWayManager.destroy()
    }

    fun call() = skyWayManager.loadAllPeerIds()

    fun hangUp() = skyWayManager.closeConnection()

    fun startLocalStream() = skyWayManager.startLocalStream()

    fun openConnection(opponentPeerId: String) = skyWayManager.openConnection(opponentPeerId)

    fun startCountDown() = viewModelScope.launch {
        _remainingTimeSecond.value = MAX_REMAINING_TIME + 1
        while (_remainingTimeSecond.value ?: 0 > 0 && connected.value == true) {
            val time = _remainingTimeSecond.value!! - 1
            _remainingTimeSecond.value = time
            skyWayManager.updateRemainingTime(time)
            Timber.d("RemainingTime = $time")

            delay(1000)
        }
        if (connected.value == true) hangUp()
    }

    companion object {
        const val MAX_REMAINING_TIME = 15
    }
}