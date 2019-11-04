package jp.shiita.astra.ui.call

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import jp.shiita.astra.util.SkyWayManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    private val _remainingTimeSecond = MutableLiveData<Int>()

    private val onStartObserver: (Unit) -> Unit = { startCountDown() }

    init {
        skyWayManager.onStartConnectionEvent.observeForever(onStartObserver)
    }

    override fun onCleared() {
        skyWayManager.onStartConnectionEvent.removeObserver(onStartObserver)
        skyWayManager.destroy()
    }

    fun call() = skyWayManager.loadAllPeerIds()

    fun hangUp() = skyWayManager.closeConnection()

    fun startLocalStream() = skyWayManager.startLocalStream()

    fun openConnection(opponentPeerId: String) = skyWayManager.openConnection(opponentPeerId)

    private fun startCountDown() = viewModelScope.launch {
        _remainingTimeSecond.value = MAX_REMAINING_TIME
        while (_remainingTimeSecond.value ?: 0 > 0) {
            delay(1000)
            val time = _remainingTimeSecond.value!! - 1
            _remainingTimeSecond.value = time
            skyWayManager.updateRemainingTime(time)
        }
        if (connected.value == true) hangUp()
    }

    companion object {
        const val MAX_REMAINING_TIME = 15
    }
}