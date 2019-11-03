package jp.shiita.astra.ui.waiting

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.shiita.astra.util.live.UnitLiveEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class WaitingViewModel @Inject constructor() : ViewModel() {

    val startCallingEvent: LiveData<Unit>
        get() = _startCallingEvent

    private var _startCallingEvent = UnitLiveEvent()

    // TODO: 一時的な処理
    fun start() = viewModelScope.launch {
        delay(2000)
        _startCallingEvent.call()
    }
}