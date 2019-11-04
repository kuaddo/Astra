package jp.shiita.astra.ui.call

import android.Manifest
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import dagger.android.support.DaggerFragment
import jp.shiita.astra.R
import jp.shiita.astra.databinding.FragmentCallBinding
import jp.shiita.astra.databinding.ItemPeerBinding
import jp.shiita.astra.extensions.dataBinding
import jp.shiita.astra.extensions.observeNonNull
import jp.shiita.astra.ui.common.DataBoundListAdapter
import jp.shiita.astra.ui.common.SimpleDiffUtil
import jp.shiita.astra.util.SkyWayManager
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.OnShowRationale
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject

@RuntimePermissions
class CallFragment : DaggerFragment() {
    @Inject
    lateinit var skyWayManager: SkyWayManager   // TODO: ViewModelに移動
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: CallViewModel by viewModels { viewModelFactory }
    private val binding by dataBinding<FragmentCallBinding>(R.layout.fragment_call)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.skyWayManager = skyWayManager
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.callButton.setOnClickListener { button ->
            button.isEnabled = false
            if (skyWayManager.connected.value == true) {
                skyWayManager.closeConnection()
            } else skyWayManager.loadAllPeerIds()
            button.isEnabled = true
        }
        observe()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().volumeControlStream = AudioManager.STREAM_VOICE_CALL
    }

    override fun onPause() {
        requireActivity().volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE
        super.onPause()
    }

    override fun onDestroyView() {
        skyWayManager.destroy()
        super.onDestroyView()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    fun startLocalStream() = skyWayManager.startLocalStream()

    @OnShowRationale(Manifest.permission.RECORD_AUDIO)
    fun showRationaleForContacts(request: PermissionRequest) {
        MaterialDialog(requireContext()).show {
            title(R.string.permission_microphone_title)
            message(R.string.permission_microphone_message)
            positiveButton(R.string.ok) { request.proceed() }
            cancelable(false)
        }
    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    fun onContactsDenied() {
        MaterialDialog(requireContext()).show {
            message(R.string.permission_microphone_denied)
            positiveButton(R.string.ok) { findNavController().popBackStack() }
            cancelable(false)
        }
    }

    @OnNeverAskAgain(Manifest.permission.RECORD_AUDIO)
    fun onContactsNeverAskAgain() {
        MaterialDialog(requireContext()).show {
            message(R.string.permission_microphone_never_ask)
            positiveButton(R.string.ok) { startAppSettingActivity() }
            negativeButton(R.string.back)
            onDismiss { findNavController().popBackStack() }
            cancelable(false)
        }
    }

    private fun observe() {
        skyWayManager.ownId.observeNonNull(viewLifecycleOwner) { startLocalStreamWithPermissionCheck() }
        skyWayManager.allPeerIds.observeNonNull(viewLifecycleOwner) { peerIds ->
            // TODO: tmp
            if (peerIds.isNotEmpty()) showPeerIds(peerIds)
            else {
                Toast.makeText(
                    requireContext(),
                    "PeerID list (other than your ID) is empty.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        skyWayManager.onStartConnectionEvent.observeNonNull(viewLifecycleOwner) { viewModel.startCountDown() }
        skyWayManager.onStopConnectionEvent.observeNonNull(viewLifecycleOwner) { showFinishDialog() }
    }

    private fun showPeerIds(peerIds: List<String>) = MaterialDialog(requireContext()).show {
        customView(R.layout.dialog_peer_list, noVerticalPadding = true)
        view.findViewById<RecyclerView>(R.id.recyclerView).also {
            it.adapter = PeerAdapter(viewLifecycleOwner) { opponentPeerId ->
                skyWayManager.openConnection(opponentPeerId)
                dismiss()
            }.apply {
                submitList(peerIds)
            }
        }
    }

    private fun showFinishDialog() = MaterialDialog(requireContext()).show {
        title(R.string.call_finish_dialog_title)
        message(R.string.call_finish_dialog_message)
        positiveButton(R.string.ok) { findNavController().popBackStack() }
        cancelable(false)
    }

    private fun startAppSettingActivity() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${requireContext().packageName}")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private class PeerAdapter(
        lifecycleOwner: LifecycleOwner,
        private val onClick: ((String) -> Unit)
    ) : DataBoundListAdapter<String, ItemPeerBinding>(lifecycleOwner, SimpleDiffUtil()) {
        override fun createBinding(parent: ViewGroup): ItemPeerBinding =
            ItemPeerBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        override fun bind(binding: ItemPeerBinding, item: String) {
            binding.peerId = item
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
