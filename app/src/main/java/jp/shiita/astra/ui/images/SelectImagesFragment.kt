package jp.shiita.astra.ui.images

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.android.support.DaggerFragment
import jp.shiita.astra.R
import jp.shiita.astra.databinding.FragmentSelectImagesBinding
import jp.shiita.astra.extensions.assistedViewModels
import jp.shiita.astra.extensions.dataBinding
import jp.shiita.astra.extensions.observeNonNull
import jp.shiita.astra.extensions.setupSnackbar
import jp.shiita.astra.extensions.setupToast
import jp.shiita.astra.extensions.showOnContactsDeniedDialog
import jp.shiita.astra.extensions.showOnContactsNeverAskAgainDialog
import jp.shiita.astra.extensions.showRationaleForContactsDialog
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.OnShowRationale
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject

@RuntimePermissions
class SelectImagesFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: SelectImagesViewModel.Factory

    private val viewModel: SelectImagesViewModel by assistedViewModels {
        viewModelFactory.create(args.imageShareId)
    }
    private val args by navArgs<SelectImagesFragmentArgs>()
    private val binding by dataBinding<FragmentSelectImagesBinding>(R.layout.fragment_select_images)
    private lateinit var adapter: ImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ImageAdapter(viewLifecycleOwner)
        binding.recyclerView.adapter = adapter
        binding.backIcon.setOnClickListener { findNavController().popBackStack() }
        binding.toolbar.apply {
            inflateMenu(R.menu.select_images)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.post -> {
                        viewModel.postSelectedImages()
                        true
                    }
                    else -> false
                }
            }
        }

        observe()
    }

    private fun observe() {
        setupToast(viewModel.toastEvent)

        viewModel.uploadFinishedEvent.observeNonNull(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
        observeImagesWithPermissionCheck()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun observeImages() {
        viewModel.images.observeNonNull(viewLifecycleOwner) { adapter.submitList(it) }
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun showRationaleForContacts(request: PermissionRequest) {
        showRationaleForContactsDialog(
            request,
            R.string.permission_storage_title,
            R.string.permission_storage_message
        )
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onContactsDenied() {
        showOnContactsDeniedDialog(R.string.permission_storage_denied)
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun onContactsNeverAskAgain() {
        showOnContactsNeverAskAgainDialog(R.string.permission_storage_never_ask)
    }
}