package jp.shiita.astra.delegate

import androidx.lifecycle.LiveData
import jp.shiita.astra.util.ToastMessage
import jp.shiita.astra.util.live.LiveEvent

interface ToastViewModelDelegate {
    val toastEvent: LiveData<ToastMessage>

    fun postMessage(message: ToastMessage)
}

class LiveEventToastViewModelDelegate : ToastViewModelDelegate {
    override val toastEvent: LiveData<ToastMessage>
        get() = _toastEvent

    private val _toastEvent = LiveEvent<ToastMessage>()

    override fun postMessage(message: ToastMessage) {
        _toastEvent.postValue(message)
    }
}