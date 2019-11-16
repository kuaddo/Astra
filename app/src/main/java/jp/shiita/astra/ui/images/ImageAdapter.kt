package jp.shiita.astra.ui.images

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import jp.shiita.astra.databinding.ItemImageBinding
import jp.shiita.astra.model.ImageItem
import jp.shiita.astra.ui.common.DataBoundListAdapter
import jp.shiita.astra.ui.common.SimpleDiffUtil

class ImageAdapter(
    lifecycleOwner: LifecycleOwner
) : DataBoundListAdapter<ImageItem, ItemImageBinding>(lifecycleOwner, SimpleDiffUtil()) {
    override fun createBinding(parent: ViewGroup): ItemImageBinding =
        ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun bind(binding: ItemImageBinding, item: ImageItem) {
        binding.imageItem = item
        binding.root.setOnClickListener { item.toggle() }
    }
}