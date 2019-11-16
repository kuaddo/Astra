package jp.shiita.astra.ui.images

import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import jp.shiita.astra.AstraApp
import jp.shiita.astra.model.ImageItem
import jp.shiita.astra.repository.AstraRepository
import kotlinx.coroutines.Dispatchers
import java.io.File
import javax.inject.Inject

class SelectImagesViewModel @Inject constructor(
    application: AstraApp,
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

    fun postSelectedImages() {
        val selectedImages = images.value?.filter { it.selected.get() }

    }
}