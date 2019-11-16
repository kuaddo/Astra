package jp.shiita.astra.ui.images

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import jp.shiita.astra.databinding.ItemViewImageBinding
import jp.shiita.astra.ui.common.DataBoundListAdapter
import jp.shiita.astra.ui.common.SimpleDiffUtil

class ViewImageAdapter(
    lifecycleOwner: LifecycleOwner
) : DataBoundListAdapter<Bitmap, ItemViewImageBinding>(lifecycleOwner, SimpleDiffUtil()) {
    override fun createBinding(parent: ViewGroup): ItemViewImageBinding =
        ItemViewImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun bind(binding: ItemViewImageBinding, item: Bitmap) {
        binding.bitmap = item
    }
}