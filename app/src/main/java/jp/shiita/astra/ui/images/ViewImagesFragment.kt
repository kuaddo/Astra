package jp.shiita.astra.ui.images

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerFragment
import jp.shiita.astra.R
import jp.shiita.astra.databinding.FragmentViewImagesBinding
import jp.shiita.astra.extensions.dataBinding
import jp.shiita.astra.extensions.observeNonNull
import javax.inject.Inject

class ViewImagesFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ViewImagesViewModel by viewModels { viewModelFactory }
    private val binding by dataBinding<FragmentViewImagesBinding>(R.layout.fragment_view_images)
    private lateinit var adapter: ViewImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ViewImageAdapter(viewLifecycleOwner)
        binding.viewPager.adapter = adapter

        observe()
        viewModel.loadImages()
    }

    private fun observe() {
        viewModel.images.observeNonNull(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}