package jp.shiita.astra.ui.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import jp.shiita.astra.model.ErrorResource
import jp.shiita.astra.model.SuccessResource
import jp.shiita.astra.repository.AstraRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class ViewImagesViewModel @AssistedInject constructor(
    @Assisted private val imageShareId: String,
    private val repository: AstraRepository
) : ViewModel() {

    @AssistedInject.Factory
    interface Factory {
        fun create(imageShareId: String): ViewImagesViewModel
    }

    val images: LiveData<List<Bitmap>>
        get() = _images

    private val _images = MutableLiveData<List<Bitmap>>()

    fun loadImages() = viewModelScope.launch {
        when (val res = repository.getImages(imageShareId)) {
            is SuccessResource -> {
                _images.value = res.data.map { it.toBitmap() }
            }
            is ErrorResource -> Timber.d("get image error imageShareId = $imageShareId")
        }
    }

    private fun ByteArray.toBitmap() = BitmapFactory.decodeByteArray(this, 0, size)
}