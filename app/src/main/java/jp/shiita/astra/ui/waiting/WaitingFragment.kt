package jp.shiita.astra.ui.waiting

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import dagger.android.support.DaggerFragment
import jp.shiita.astra.R
import jp.shiita.astra.databinding.FragmentWaitingBinding
import jp.shiita.astra.extensions.dataBinding
import jp.shiita.astra.extensions.observeNonNull
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.OnShowRationale
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject

@RuntimePermissions
class WaitingFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: WaitingViewModel by viewModels { viewModelFactory }
    private val binding by dataBinding<FragmentWaitingBinding>(R.layout.fragment_waiting)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        observe()
    }

    override fun onResume() {
        super.onResume()
        viewModel.start()
    }

    private fun observe() {
        viewModel.startCallingEvent.observeNonNull(viewLifecycleOwner) {
            findNavController().navigate(WaitingFragmentDirections.actionWaitingToCall())
        }

        gridObserveWithPermissionCheck()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun gridObserve() {
        viewModel.startGridObserve()
    }

    // TODO: 以下流用。あとで直す
    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showRationaleForContacts(request: PermissionRequest) {
        MaterialDialog(requireContext()).show {
            title(R.string.permission_microphone_title)
            message(R.string.permission_microphone_message)
            positiveButton(R.string.ok) { request.proceed() }
            cancelable(false)
        }
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onContactsDenied() {
        MaterialDialog(requireContext()).show {
            message(R.string.permission_microphone_denied)
            positiveButton(R.string.ok) { findNavController().popBackStack() }
            cancelable(false)
        }
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onContactsNeverAskAgain() {
        MaterialDialog(requireContext()).show {
            message(R.string.permission_microphone_never_ask)
            positiveButton(R.string.ok)
            negativeButton(R.string.back)
            onDismiss { findNavController().popBackStack() }
            cancelable(false)
        }
    }
}