package jp.shiita.astra.ui.images

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.shiita.astra.R
import jp.shiita.astra.extensions.getBitmap
import jp.shiita.astra.repository.AstraRepository
import javax.inject.Inject

class ViewImagesViewModel @Inject constructor(
    private val repository: AstraRepository,
    private val context: Context    // TODO: test
) : ViewModel() {
    val images: LiveData<List<Bitmap>>
        get() = _images

    private val _images = MutableLiveData<List<Bitmap>>()

    fun loadImages() {
        val ics = listOf(R.drawable.ic_back, R.drawable.ic_hang_up, R.drawable.ic_download)
        val drawables = ics.map { ResourcesCompat.getDrawable(context.resources, it, null) }
        _images.value = drawables.map { it!!.getBitmap() }
    }
}