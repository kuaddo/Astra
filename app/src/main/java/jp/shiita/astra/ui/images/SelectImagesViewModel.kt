package jp.shiita.astra.ui.images

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.request.transition.Transition
import jp.shiita.astra.AstraApp
import jp.shiita.astra.extensions.toBytes
import jp.shiita.astra.model.ErrorResource
import jp.shiita.astra.model.ImageItem
import jp.shiita.astra.model.SuccessResource
import jp.shiita.astra.repository.AstraRepository
import jp.shiita.astra.util.GlideApp
import jp.shiita.astra.util.SimpleBitmapTarget
import jp.shiita.astra.util.live.UnitLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class SelectImagesViewModel @Inject constructor(
    private val application: AstraApp,
    private val repository: AstraRepository
) : ViewModel() {
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

    val uploadFinishedEvent: LiveData<Unit>
        get() = _uploadFinishedEvent

    private val _uploadFinishedEvent = UnitLiveEvent()

    private var count = AtomicInteger(-1)

    fun postSelectedImages() {
        if (count.get() != -1) return

        val selectedUris = images.value
            ?.filter { it.selected.get() }
            ?.map { it.uri } ?: return
        count.set(selectedUris.size)

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
        // TODO: test id
        val id = "testId"
        when (val res = repository.postImage(id, byteArray)) {
            is SuccessResource -> {
            }
            is ErrorResource -> Timber.d("post image error SkyWayID = $id")
        }
        if (count.decrementAndGet() == 0) _uploadFinishedEvent.call()
    }
}