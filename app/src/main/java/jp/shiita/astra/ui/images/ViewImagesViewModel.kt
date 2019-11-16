package jp.shiita.astra.ui.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.shiita.astra.model.ErrorResource
import jp.shiita.astra.model.SuccessResource
import jp.shiita.astra.repository.AstraRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ViewImagesViewModel @Inject constructor(
    private val repository: AstraRepository
) : ViewModel() {
    val images: LiveData<List<Bitmap>>
        get() = _images

    private val _images = MutableLiveData<List<Bitmap>>()

    fun loadImages() = viewModelScope.launch {
        // TODO: test
        val opponentId = "testId"
        when (val res = repository.getImages(opponentId)) {
            is SuccessResource -> {
                _images.value = res.data.map { it.toBitmap() }
            }
            is ErrorResource -> Timber.d("get image error opponentId = $opponentId")
        }
    }

    private fun ByteArray.toBitmap() = BitmapFactory.decodeByteArray(this, 0, size)
}