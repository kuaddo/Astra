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
import jp.shiita.astra.extensions.dataBinding
import jp.shiita.astra.ui.PeerListDialogFragment
import org.json.JSONArray
import timber.log.Timber

class CallFragment : DaggerFragment() {
    private val binding by dataBinding<FragmentCallBinding>(R.layout.fragment_call)

    private var peer: Peer? = null
    private var localStream: MediaStream? = null
    private var remoteStream: MediaStream? = null
    private var mediaConnection: MediaConnection? = null

    private var ownId: String = ""
    private var connected: Boolean = false

    private lateinit var handler: Handler

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler = Handler(Looper.getMainLooper())
        peer = Peer(context, PeerOption().apply {
            key = getString(R.string.sky_way_api_key)
            domain = getString(R.string.sky_way_domain)
            debug = Peer.DebugLevelEnum.ALL_LOGS
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setPeerCallbacks()

        binding.callButton.setOnClickListener { button ->
            button.isEnabled = false
            if (connected) {
                closeRemoteStream()
                mediaConnection?.close()
                mediaConnection = null
            } else {
                showPeerIDs()
            }
            button.isEnabled = true
        }
        binding.switchButton.setOnClickListener { localStream?.switchCamera() }
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
        localStream = Navigator.getUserMedia(MediaConstraints())
        localStream?.addVideoRenderer(binding.localWindow, 0)
    }


    private fun setMediaCallbacks() {
        mediaConnection?.on(MediaConnection.MediaEventEnum.STREAM) {
            remoteStream = it as MediaStream
            remoteStream?.addVideoRenderer(binding.remoteWindow, 0)
        }
        mediaConnection?.on(MediaConnection.MediaEventEnum.CLOSE) {
            closeRemoteStream()
            connected = false
            updateActionButtonTitle()
        }
        mediaConnection?.on(MediaConnection.MediaEventEnum.ERROR) {
            Timber.e(it as PeerError, "[On/MediaError]")
        }
    }

    private fun destroyPeer() {
        closeRemoteStream()

        localStream?.removeVideoRenderer(binding.localWindow, 0)
        localStream?.close()

        mediaConnection?.let { connection ->
            if (connection.isOpen) connection.close()
            unsetMediaCallbacks()
        }

        Navigator.terminate()

        unsetPeerCallback()
        peer?.let { p ->
            if (!p.isDisconnected) p.disconnect()
            if (!p.isDestroyed) p.destroy()
        }
        peer = null
    }

    private fun setPeerCallbacks() {
        peer?.let { p ->
            p.on(Peer.PeerEventEnum.OPEN) { id ->
                ownId = id as String
                binding.ownIdText.text = ownId

                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                        REQUEST_CODE_PERMISSION
                    )
                } else {
                    startLocalStream()
                }
            }

            p.on(Peer.PeerEventEnum.CALL) {
                mediaConnection = it as? MediaConnection ?: return@on

                setMediaCallbacks()
                mediaConnection?.answer(localStream)

                connected = true
                updateActionButtonTitle()
            }

            p.on(Peer.PeerEventEnum.ERROR) {
                Timber.e(it as PeerError, "[On/Error]")
            }

            p.on(Peer.PeerEventEnum.CLOSE) {
                Timber.d("[On/Close]")
            }

            p.on(Peer.PeerEventEnum.DISCONNECTED) {
                Timber.d("[On/Disconnected]")
            }
        }
    }

    private fun unsetPeerCallback() {
        peer?.let { p ->
            p.on(Peer.PeerEventEnum.OPEN, null)
            p.on(Peer.PeerEventEnum.CONNECTION, null)
            p.on(Peer.PeerEventEnum.CALL, null)
            p.on(Peer.PeerEventEnum.CLOSE, null)
            p.on(Peer.PeerEventEnum.DISCONNECTED, null)
            p.on(Peer.PeerEventEnum.ERROR, null)
        }
    }

    private fun unsetMediaCallbacks() {
        mediaConnection?.let { connection ->
            connection.on(MediaConnection.MediaEventEnum.STREAM, null)
            connection.on(MediaConnection.MediaEventEnum.CLOSE, null)
            connection.on(MediaConnection.MediaEventEnum.ERROR, null)
        }
    }

    private fun closeRemoteStream() {
        remoteStream?.removeVideoRenderer(binding.remoteWindow, 0)
        remoteStream?.close()
    }

    private fun onPeerSelected(peerId: String) {
        mediaConnection?.close()

        val option = CallOption()
        mediaConnection = peer?.call(peerId, localStream, option)
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
        peer?.listAllPeers(OnCallback { any ->
            // TODO: moshiでパース
            val json = any as? JSONArray ?: return@OnCallback

            val peerIds = arrayListOf<String>()
            (0 until json.length()).forEach {
                peerIds.add(json.getString(it))
            }
            peerIds.remove(ownId)

            if (peerIds.isNotEmpty()) {
                // TODO: material dialog使う
                PeerListDialogFragment().apply {
                    setListener { item -> handler.post { onPeerSelected(item) } }
                    setItems(peerIds)
                    show(parentFragmentManager, "peerlist")
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

    companion object {
        private const val REQUEST_CODE_PERMISSION = 1000
    }
}
