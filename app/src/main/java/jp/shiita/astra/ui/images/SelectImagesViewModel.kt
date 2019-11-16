package jp.shiita.astra.ui.images

import androidx.lifecycle.ViewModel
import jp.shiita.astra.repository.AstraRepository
import javax.inject.Inject

class SelectImagesViewModel @Inject constructor(
    private val repository: AstraRepository
) : ViewModel() {

}