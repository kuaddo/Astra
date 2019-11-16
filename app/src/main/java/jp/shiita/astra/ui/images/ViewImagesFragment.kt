package jp.shiita.astra.ui.images

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.android.support.DaggerFragment
import jp.shiita.astra.R
import jp.shiita.astra.databinding.FragmentViewImagesBinding
import jp.shiita.astra.extensions.assistedViewModels
import jp.shiita.astra.extensions.dataBinding
import jp.shiita.astra.extensions.observeNonNull
import javax.inject.Inject

class ViewImagesFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewImagesViewModel.Factory

    private val viewModel: ViewImagesViewModel by assistedViewModels { viewModelFactory.create(args.imageShareId) }
    private val args by navArgs<ViewImagesFragmentArgs>()
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
        binding.backIcon.setOnClickListener { findNavController().popBackStack() }

        observe()
        viewModel.loadImages()
    }

    private fun observe() {
        viewModel.images.observeNonNull(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}