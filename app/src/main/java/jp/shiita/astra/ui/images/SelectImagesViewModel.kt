package jp.shiita.astra.ui.images

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.request.transition.Transition
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import jp.shiita.astra.AstraApp
import jp.shiita.astra.R
import jp.shiita.astra.delegate.LoadingViewModelDelegate
import jp.shiita.astra.delegate.SnackbarViewModelDelegate
import jp.shiita.astra.delegate.ToastViewModelDelegate
import jp.shiita.astra.extensions.toBytes
import jp.shiita.astra.model.ErrorResource
import jp.shiita.astra.model.ImageItem
import jp.shiita.astra.model.SuccessResource
import jp.shiita.astra.repository.AstraRepository
import jp.shiita.astra.util.GlideApp
import jp.shiita.astra.util.SimpleBitmapTarget
import jp.shiita.astra.util.ToastMessageRes
import jp.shiita.astra.util.live.UnitLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class SelectImagesViewModel @AssistedInject constructor(
    @Assisted private val imageShareId: String,
    private val application: AstraApp,
    private val repository: AstraRepository,
    loadingViewModelDelegate: LoadingViewModelDelegate,
    toastViewModelDelegate: ToastViewModelDelegate,
    snackbarViewModelDelegate: SnackbarViewModelDelegate
) : ViewModel(),
    LoadingViewModelDelegate by loadingViewModelDelegate,
    ToastViewModelDelegate by toastViewModelDelegate,
    SnackbarViewModelDelegate by snackbarViewModelDelegate {

    @AssistedInject.Factory
    interface Factory {
        fun create(imageShareId: String): SelectImagesViewModel
    }

    val images: LiveData<List<ImageItem>> = liveData(Dispatchers.IO) {

        application.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.ImageColumns.DATA),
            null,
            null,
            null
        )?.use { cursor ->
            // if the content provider returns null in an unexpected situation
            // https://stackoverflow.com/questions/13080540/what-causes-androids-contentresolver-query-to-return-null
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            val list = mutableListOf<ImageItem>()
            while (cursor.moveToNext()) {
                val filePath = cursor.getString(index)
                list.add(0, ImageItem(Uri.fromFile(File(filePath))))
            }
            emit(list.toList())
        }
    }
    val remainingCountText: LiveData<String>
        get() = _remainingCountText

    val uploadFinishedEvent: LiveData<Unit>
        get() = _uploadFinishedEvent

    private val _remainingCountText = MutableLiveData<String>()
    private val _uploadFinishedEvent = UnitLiveEvent()

    private var imagesCount = 0
    private val remainingCount = AtomicInteger(-1)
    private val successCount = AtomicInteger(0)

    fun postSelectedImages() {
        if (remainingCount.get() != -1) return
        if (isLoading.value == true) return
        startViewModelLoading()

        val selectedUris = images.value
            ?.filter { it.selected.get() }
            ?.map { it.uri } ?: return
        imagesCount = selectedUris.size
        remainingCount.set(imagesCount)

        selectedUris.forEach {
            GlideApp.with(application.applicationContext)
                .asBitmap()
                .load(it)
                .into(object : SimpleBitmapTarget(500, 500) {   // TODO: 適切なサイズを求める
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        postSelectedImage(resource.toBytes())
                    }
                })
        }
    }

    private fun postSelectedImage(byteArray: ByteArray) = viewModelScope.launch {
        when (repository.postImage(imageShareId, byteArray)) {
            is SuccessResource -> {
                val count = successCount.incrementAndGet()
                _remainingCountText.value = application.getString(
                    R.string.select_images_post_count,
                    count,
                    imagesCount
                )
            }
            is ErrorResource -> Timber.d("post image error imageShareId = $imageShareId")
        }
        if (remainingCount.decrementAndGet() == 0) {
            postMessage(ToastMessageRes(R.string.select_images_post_finished))
            _uploadFinishedEvent.call()
        }
    }
}