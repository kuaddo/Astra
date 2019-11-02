package jp.shiita.astra.delegate

import androidx.lifecycle.LiveData
import jp.shiita.astra.util.SnackbarMessage
import jp.shiita.astra.util.live.LiveEvent

interface SnackbarViewModelDelegate {
    val snackbarEvent: LiveData<SnackbarMessage>

    fun postMessage(message: SnackbarMessage)
}

class LiveEventSnackbarViewModelDelegate : SnackbarViewModelDelegate {
    override val snackbarEvent: LiveData<SnackbarMessage>
        get() = _snackbarEvent

    private val _snackbarEvent = LiveEvent<SnackbarMessage>()

    override fun postMessage(message: SnackbarMessage) {
        _snackbarEvent.postValue(message)
    }
}