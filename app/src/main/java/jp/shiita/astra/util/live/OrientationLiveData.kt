package jp.shiita.astra.util.live

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import jp.shiita.astra.model.celestialsphere.DeviceOrientation
import javax.inject.Inject

/**
 * LiveDataとイベントリスナーを用いて端末の方向を監視するためのクラス
 * 監視されている端末のオイラー角を入手するにはActivityにおいて以下のように記述すればよい
 * OrientationLiveData(this).observe(this, Observer {
 *      // itはDeviceOrientationインスタンス
 *      if (it == null) return@Observer
 *
 *      // itがnullでない場合にはオイラー角が取得できる
 * })
 */
class OrientationLiveData @Inject constructor(
    private val mSensorManager: SensorManager?
) : LiveData<DeviceOrientation>(), SensorEventListener {
    private val sensorDelay: Int = SensorManager.SENSOR_DELAY_UI

    // センサー群
    private val accelerometer = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticField = mSensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val mAccelerometerReading = FloatArray(VECTOR_DIM)
    private val mMagnetometerReading = FloatArray(VECTOR_DIM)

    private val mRotationMatrix = FloatArray(MATRIX_DIM)
    private val mOrientationAngles = FloatArray(VECTOR_DIM)

    override fun onActive() {
        super.onActive()

        mSensorManager?.registerListener(
            this, accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL, sensorDelay
        )
        mSensorManager?.registerListener(
            this, magneticField,
            SensorManager.SENSOR_DELAY_NORMAL, sensorDelay
        )
    }

    override fun onInactive() {
        super.onInactive()
        mSensorManager?.unregisterListener(this)
    }

    /**
     * 何もしない
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when (event.sensor) {
                accelerometer -> {
                    System.arraycopy(
                        event.values, 0,
                        mAccelerometerReading, 0, VECTOR_DIM
                    )
                }
                magneticField -> {
                    System.arraycopy(
                        event.values, 0,
                        mMagnetometerReading, 0, VECTOR_DIM
                    )
                }
            }

            updateOrientationAngles()
            value = DeviceOrientation(
                mOrientationAngles[0],
                mOrientationAngles[1],
                mOrientationAngles[2]
            )
        }
    }

    /**
     * 加速度センサーと磁気センサーから端末のオイラー角を計算
     */
    private fun updateOrientationAngles() {
        SensorManager.getRotationMatrix(
            mRotationMatrix, null,
            mAccelerometerReading, mMagnetometerReading
        )
        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles)
    }

    companion object {
        private const val VECTOR_DIM = 3
        private const val MATRIX_DIM = 9
    }
}