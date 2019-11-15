package jp.shiita.astra.ui

import android.media.AudioManager
import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import jp.shiita.astra.R

class MainActivity : DaggerAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_VOICE_CALL
    }

    override fun onPause() {
        super.onPause()
        volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE
    }
}
