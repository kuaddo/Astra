package jp.shiita.astra.ui.call

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import dagger.android.support.DaggerFragment
import io.skyway.Peer.Browser.MediaConstraints
import io.skyway.Peer.Browser.MediaStream
import io.skyway.Peer.Browser.Navigator
import io.skyway.Peer.CallOption
import io.skyway.Peer.MediaConnection
import io.skyway.Peer.OnCallback
import io.skyway.Peer.Peer
import io.skyway.Peer.PeerError
import io.skyway.Peer.PeerOption
import jp.shiita.astra.R
import jp.shiita.astra.databinding.FragmentCallBinding
import jp.shiita.astra.databinding.ItemPeerBinding
import jp.shiita.astra.extensions.dataBinding
import jp.shiita.astra.ui.common.DataBoundListAdapter
import jp.shiita.astra.ui.common.SimpleDiffUtil
import org.json.JSONArray
import timber.log.Timber

class CallFragment : DaggerFragment() {
    private val binding by dataBinding<FragmentCallBinding>(R.layout.fragment_call)

    private lateinit var handler: Handler
    private lateinit var peer: Peer
    private lateinit var localStream: MediaStream

    private var remoteStream: MediaStream? = null
    private var mediaConnection: MediaConnection? = null

    private var ownId: String = ""
    private var connected: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler = Handler(Looper.getMainLooper())
        peer = Peer(context, PeerOption().apply {
            key = getString(R.string.sky_way_api_key)
            domain = getString(R.string.sky_way_domain)
            debug = Peer.DebugLevelEnum.ALL_LOGS
        })
        setPeerCallbacks()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.callButton.setOnClickListener { button ->
            button.isEnabled = false
            if (connected) {
                remoteStream?.close()
                mediaConnection?.close()
                mediaConnection = null
            } else {
                showPeerIDs()
            }
            button.isEnabled = true
        }
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
        destroyPeer()
        super.onDestroyView()
    }

    // TODO: permission dispatcherを利用する
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocalStream()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to access the camera and microphone.\nclick allow when asked for permission.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun startLocalStream() {
        Navigator.initialize(peer)

        val constraints = MediaConstraints().apply {
            audioFlag = true
            videoFlag = false
        }
        localStream = Navigator.getUserMedia(constraints)
    }


    private fun setMediaCallbacks() {
        mediaConnection?.on(MediaConnection.MediaEventEnum.STREAM) {
            remoteStream = it as MediaStream
        }
        mediaConnection?.on(MediaConnection.MediaEventEnum.CLOSE) {
            remoteStream?.close()
            connected = false
            updateActionButtonTitle()
        }
        mediaConnection?.on(MediaConnection.MediaEventEnum.ERROR) {
            Timber.e(it as PeerError, "[On/MediaError]")
        }
    }

    private fun destroyPeer() {
        remoteStream?.close()
        localStream.close()

        mediaConnection?.let { connection ->
            if (connection.isOpen) connection.close()
            unsetMediaCallbacks()
        }

        Navigator.terminate()

        unsetPeerCallback()
        if (!peer.isDisconnected) peer.disconnect()
        if (!peer.isDestroyed) peer.destroy()
    }

    private fun setPeerCallbacks() {
        peer.on(Peer.PeerEventEnum.OPEN) { id ->
            ownId = id as String
            binding.ownIdText.text = ownId

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_CODE_PERMISSION
                )
            } else {
                startLocalStream()
            }
        }

        peer.on(Peer.PeerEventEnum.CALL) {
            mediaConnection = it as? MediaConnection ?: return@on

            setMediaCallbacks()
            mediaConnection?.answer(localStream)

            connected = true
            updateActionButtonTitle()
        }

        peer.on(Peer.PeerEventEnum.ERROR) {
            Timber.e(it as PeerError, "[On/Error]")
        }

        peer.on(Peer.PeerEventEnum.CLOSE) {
            Timber.d("[On/Close]")
        }

        peer.on(Peer.PeerEventEnum.DISCONNECTED) {
            Timber.d("[On/Disconnected]")
        }
    }

    private fun unsetPeerCallback() {
        peer.on(Peer.PeerEventEnum.OPEN, null)
        peer.on(Peer.PeerEventEnum.CONNECTION, null)
        peer.on(Peer.PeerEventEnum.CALL, null)
        peer.on(Peer.PeerEventEnum.CLOSE, null)
        peer.on(Peer.PeerEventEnum.DISCONNECTED, null)
        peer.on(Peer.PeerEventEnum.ERROR, null)
    }

    private fun unsetMediaCallbacks() {
        mediaConnection?.let { connection ->
            connection.on(MediaConnection.MediaEventEnum.STREAM, null)
            connection.on(MediaConnection.MediaEventEnum.CLOSE, null)
            connection.on(MediaConnection.MediaEventEnum.ERROR, null)
        }
    }

    private fun onPeerSelected(peerId: String) {
        mediaConnection?.close()

        val option = CallOption()
        mediaConnection = peer.call(peerId, localStream, option)
        if (mediaConnection != null) {
            setMediaCallbacks()
            connected = true
        }

        updateActionButtonTitle()
    }

    private fun showPeerIDs() {
        if (ownId.isBlank()) {
            Toast.makeText(requireContext(), "Your PeerID is null or invalid.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Get all IDs connected to the server
        peer.listAllPeers(OnCallback { any ->
            val json = any as? JSONArray ?: return@OnCallback

            val peerIds = arrayListOf<String>()
            (0 until json.length()).forEach {
                peerIds.add(json.getString(it))
            }
            peerIds.remove(ownId)

            if (peerIds.isNotEmpty()) {
                MaterialDialog(requireContext()).show {
                    customView(R.layout.dialog_peer_list, noVerticalPadding = true)
                    view.findViewById<RecyclerView>(R.id.recyclerView).also {
                        it.adapter = PeerAdapter(viewLifecycleOwner) {
                            dismiss()
                            handler.post { onPeerSelected(it) }
                        }.apply {
                            submitList(peerIds)
                        }
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "PeerID list (other than your ID) is empty.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateActionButtonTitle() {
        handler.post {
            binding.callButton.text = if (connected) "Hang up" else "Make Call"
        }
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

    companion object {
        private const val REQUEST_CODE_PERMISSION = 1000
    }
}
