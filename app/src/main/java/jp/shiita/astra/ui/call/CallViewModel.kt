package jp.shiita.astra.ui.call

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class CallViewModel @Inject constructor() : ViewModel() {

    val restTimeSecond: LiveData<Int>
        get() = _restTimeSecond

    private val _restTimeSecond = MutableLiveData<Int>()

    fun startCountDown() = viewModelScope.launch {
        _restTimeSecond.value = TIME_LIMIT_SECOND
        while (_restTimeSecond.value ?: 0 > 0) {
            delay(1000)
            _restTimeSecond.value = _restTimeSecond.value!! - 1
        }
    }

    companion object {
        private const val TIME_LIMIT_SECOND = 15
    }
}