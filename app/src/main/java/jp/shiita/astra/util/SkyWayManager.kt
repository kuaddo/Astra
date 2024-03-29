package jp.shiita.astra.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.skyway.Peer.Browser.MediaConstraints
import io.skyway.Peer.Browser.MediaStream
import io.skyway.Peer.Browser.Navigator
import io.skyway.Peer.CallOption
import io.skyway.Peer.MediaConnection
import io.skyway.Peer.Peer
import io.skyway.Peer.PeerError
import io.skyway.Peer.PeerOption
import jp.shiita.astra.AstraApp
import jp.shiita.astra.R
import jp.shiita.astra.ui.CallViewModel.Companion.MAX_REMAINING_TIME
import jp.shiita.astra.util.live.UnitLiveEvent
import timber.log.Timber
import javax.inject.Inject

class SkyWayManager @Inject constructor(
    private val application: AstraApp,
    private val notificationManager: AstraNotificationManager
) {

    val ownId: LiveData<String>
        get() = _ownId
    val imageShareId: LiveData<String>  // callした側のSkyWayId
        get() = _imageShareId
    val connected: LiveData<Boolean>
        get() = _connected
    val onStartConnectionEvent: LiveData<Unit>
        get() = _onStartConnectionEvent
    val onStopConnectionEvent: LiveData<Unit>
        get() = _onStopConnectionEvent

    var isStartedLocalStream = false
        private set

    private val _ownId = MutableLiveData<String>()
    private val _imageShareId = MutableLiveData<String>()
    private val _connected = MutableLiveData<Boolean>().apply { value = false }

    private val _onStartConnectionEvent = UnitLiveEvent()
    private val _onStopConnectionEvent = UnitLiveEvent()

    private val closeObserver: (Unit) -> Unit = { closeConnection() }

    private val peer = application.applicationContext.let { context ->
        Peer(context, PeerOption().apply {
            key = context.getString(R.string.sky_way_api_key)
            domain = context.getString(R.string.sky_way_domain)
            debug = Peer.DebugLevelEnum.ALL_LOGS
        })
    }

    private var localStream: MediaStream? = null
    private var remoteStream: MediaStream? = null
    private var mediaConnection: MediaConnection? = null

    init {
        setPeerCallbacks()
        application.closeSkyWayManagerEvent.observeForever(closeObserver)
    }

    fun startLocalStream() {
        Navigator.initialize(peer)

        val constraints = MediaConstraints().apply {
            audioFlag = true
            videoFlag = false
        }
        localStream = Navigator.getUserMedia(constraints)
        isStartedLocalStream = true
    }

    fun openConnection(opponentPeerId: String) =
        peer.call(opponentPeerId, localStream, CallOption())?.let {
            setUpMediaConnection(it, false)
        }

    fun closeConnection() {
        remoteStream?.close()
        mediaConnection?.let { connection ->
            if (connection.isOpen) connection.close()
            unsetMediaCallbacks(connection)
        }
        remoteStream = null
        mediaConnection = null

        if (_connected.value == true) {
            _connected.value = false
            _onStopConnectionEvent.call()
            notificationManager.cancelInTalkNotification()
        }
    }

    fun destroy() {
        application.closeSkyWayManagerEvent.removeObserver(closeObserver)
        localStream?.close()
        closeConnection()

        Navigator.terminate()

        unsetPeerCallback()
        if (!peer.isDisconnected) peer.disconnect()
        if (!peer.isDestroyed) peer.destroy()
    }

    fun updateRemainingTime(remainingTime: Int) =
        notificationManager.createInTalkNotification(remainingTime)

    private fun setUpMediaConnection(connection: MediaConnection, isReceived: Boolean) {
        mediaConnection = connection
        setMediaCallbacks(connection)
        if (isReceived) connection.answer(localStream)
        _imageShareId.value = if (isReceived) connection.peer() else _ownId.value
        _connected.value = true
        _onStartConnectionEvent.call()
        notificationManager.createInTalkNotification(MAX_REMAINING_TIME)
    }

    private fun setPeerCallbacks() {
        peer.on(Peer.PeerEventEnum.OPEN) { id ->
            _ownId.value = id as String
        }
        peer.on(Peer.PeerEventEnum.CALL) {
            val connection = it as? MediaConnection ?: return@on
            setUpMediaConnection(connection, true)
        }
        peer.on(Peer.PeerEventEnum.CLOSE) { Timber.d("[On/Close]") }
        peer.on(Peer.PeerEventEnum.DISCONNECTED) { Timber.d("[On/Disconnected]") }
        peer.on(Peer.PeerEventEnum.ERROR) { Timber.e(it as PeerError, "[On/Error]") }
    }

    private fun unsetPeerCallback() {
        peer.on(Peer.PeerEventEnum.OPEN, null)
        peer.on(Peer.PeerEventEnum.CALL, null)
        peer.on(Peer.PeerEventEnum.CLOSE, null)
        peer.on(Peer.PeerEventEnum.DISCONNECTED, null)
        peer.on(Peer.PeerEventEnum.ERROR, null)
    }

    private fun setMediaCallbacks(connection: MediaConnection) {
        connection.on(MediaConnection.MediaEventEnum.STREAM) {
            remoteStream = it as MediaStream
        }
        connection.on(MediaConnection.MediaEventEnum.CLOSE) {
            closeConnection()
        }
        connection.on(MediaConnection.MediaEventEnum.ERROR) {
            Timber.e(it as PeerError, "[On/MediaError]")
        }
    }

    private fun unsetMediaCallbacks(connection: MediaConnection) {
        connection.on(MediaConnection.MediaEventEnum.STREAM, null)
        connection.on(MediaConnection.MediaEventEnum.CLOSE, null)
        connection.on(MediaConnection.MediaEventEnum.ERROR, null)
    }
}