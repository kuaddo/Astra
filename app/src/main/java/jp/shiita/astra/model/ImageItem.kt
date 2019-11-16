package jp.shiita.astra.model

import android.net.Uri
import androidx.databinding.ObservableBoolean

data class ImageItem(
    val uri: Uri,
    var selected: ObservableBoolean = ObservableBoolean(false)
) {
    fun toggle() {
        selected.set(!selected.get())
    }
}