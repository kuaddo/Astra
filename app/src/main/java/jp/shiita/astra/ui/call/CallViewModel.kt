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

    val restTimeSecond: LiveData<Int>
        get() = _restTimeSecond
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

    private val _restTimeSecond = MutableLiveData<Int>()

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
        _restTimeSecond.value = TIME_LIMIT_SECOND
        while (_restTimeSecond.value ?: 0 > 0) {
            delay(1000)
            _restTimeSecond.value = _restTimeSecond.value!! - 1
        }
        if (connected.value == true) hangUp()
    }

    companion object {
        private const val TIME_LIMIT_SECOND = 15
    }
}