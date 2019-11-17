package jp.shiita.astra.util.live

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import androidx.lifecycle.LiveData
import jp.shiita.astra.model.celestialsphere.DeviceOrientation
import javax.inject.Inject
import kotlin.math.abs

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

    private data class DeviceRotationState(val isFasing: Boolean,
                                           val isStanding: Boolean,
                                           val rotation: Int)

    private val sensorDelay: Int = SensorManager.SENSOR_DELAY_UI

    // センサー群
    private val accelerometer = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticField = mSensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val mAccelerometerReading = FloatArray(VECTOR_DIM)
    private val mMagnetometerReading = FloatArray(VECTOR_DIM)

    private val mRotationMatrix = FloatArray(MATRIX_DIM)
    private val mRemappedMatrix = FloatArray(MATRIX_DIM)
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

            // TODO: 平均を取る処理等に変換する
            if (isSuspending(500)) return

            val deviceRotationState = checkDeviceState()
            adjustOrientationAngles(deviceRotationState)
            value = DeviceOrientation(
                mOrientationAngles[0],
                mOrientationAngles[1],
                mOrientationAngles[2]
            )
        }
    }

    private var beforeChangedTime = 0L

    private fun isSuspending(intervalMillis: Int): Boolean {
        val time = System.currentTimeMillis()
        if (time - beforeChangedTime > intervalMillis) {
            beforeChangedTime = time
            return false
        }
        return true
    }

    private fun checkDeviceState(): DeviceRotationState {
        val xGravity = mAccelerometerReading[0]
        val yGravity = mAccelerometerReading[1]
        val zGravity = mAccelerometerReading[2]

        var isStanding = false
        if (abs(yGravity) > abs(zGravity) || abs(xGravity) > abs(zGravity)) {
            isStanding = true
        }

        var rotation = Surface.ROTATION_0
        if (isStanding) {
            if (xGravity < -8.0f) {
                rotation = Surface.ROTATION_90
            } else if (xGravity > 8.0f) {
                rotation = Surface.ROTATION_270
            } else if (yGravity < -8.0f) {
                rotation = Surface.ROTATION_180
            }
        }

        val xMagneticMagnitude = mMagnetometerReading[0]
        val yMagneticMagnitude = mMagnetometerReading[1]
        val zMagneticMagnitude = mMagnetometerReading[2]

        var isFacing = true
        if (isStanding && (zMagneticMagnitude > abs(xMagneticMagnitude)
                    || zMagneticMagnitude > abs(yMagneticMagnitude))
            || !isStanding
            && (zMagneticMagnitude < 0 && abs(zMagneticMagnitude) < 6.0f)) {
            isFacing = false
        }
        return DeviceRotationState(isFacing, isStanding, rotation)
    }

    /**
     * 加速度センサーと磁気センサーから端末のオイラー角を計算
     */
    private fun updateOrientationAngles(newAxisX: Int, newAxisY: Int) {
        SensorManager.getRotationMatrix(
            mRotationMatrix, null,
            mAccelerometerReading, mMagnetometerReading
        )
        SensorManager.remapCoordinateSystem(
            mRotationMatrix, newAxisX, newAxisY, mRemappedMatrix
        )
        val newOrientationAngles = FloatArray(VECTOR_DIM)
        SensorManager.getOrientation(mRemappedMatrix, newOrientationAngles)
        useRcFilter(newOrientationAngles, mOrientationAngles)
    }

    /**
     * 端末の傾きを考慮して端末のオイラー角を補正
     * TODO: 他にも端末の姿勢を考えるべき
     */
    private fun adjustOrientationAngles(
        deviceRotationState: DeviceRotationState) {
        var newAxisX = SensorManager.AXIS_X
        var newAxisY = SensorManager.AXIS_Y
        when (deviceRotationState.isStanding) {
            // 端末が立っているならAzimuthとRollまたはPitchを変える必要がある
            true -> {
                when (deviceRotationState.rotation) {
                    Surface.ROTATION_0 -> {
                        newAxisY = SensorManager.AXIS_MINUS_Z
                        updateOrientationAngles(newAxisX, newAxisY)
                        // azimuthとrollを入れ替える
                        val tempAzimuth = mOrientationAngles[0]
                        mOrientationAngles[0] = -mOrientationAngles[2]
                        mOrientationAngles[2] = tempAzimuth
                    }
                    Surface.ROTATION_90 -> {
                        newAxisX = SensorManager.AXIS_Y
                        newAxisY = SensorManager.AXIS_MINUS_Z
                        updateOrientationAngles(newAxisX, newAxisY)
                        // azimuthとpitch、pitchとroll、rollとazimuthを入れ替える
                        val tempAzimuth = mOrientationAngles[0]
                        val tempPitch = mOrientationAngles[1]
                        mOrientationAngles[0] = -mOrientationAngles[2]
                        mOrientationAngles[1] = -tempAzimuth
                        mOrientationAngles[2] = tempPitch
                    }
                    Surface.ROTATION_180 -> {
                        newAxisX = SensorManager.AXIS_MINUS_X
                        newAxisY = SensorManager.AXIS_MINUS_Z
                        updateOrientationAngles(newAxisX, newAxisY)
                        val tempAzimuth = mOrientationAngles[0]
                        mOrientationAngles[0] = mOrientationAngles[2]
                        mOrientationAngles[1] = -mOrientationAngles[1]
                        mOrientationAngles[2] = tempAzimuth
                    }
                    Surface.ROTATION_270 -> {
                        newAxisX = SensorManager.AXIS_MINUS_Y
                        newAxisY = SensorManager.AXIS_MINUS_Z
                        updateOrientationAngles(newAxisX, newAxisY)
                        val tempAzimuth = mOrientationAngles[0]
                        val tempPitch = mOrientationAngles[1]
                        mOrientationAngles[0] = -mOrientationAngles[2]
                        mOrientationAngles[1] = tempAzimuth
                        mOrientationAngles[2] = -tempPitch
                    }
                }
            }
            // 端末が寝ている状態ならRollとPitchを変える必要がある
            false -> {
                updateOrientationAngles(newAxisX, newAxisY)
            }
        }
    }

    /**
     * RCフィルターでスムージング
     * @param currValues センサーで取得された現在の値
     * @param prevValues センサーで所得された前の値
     * @param alpha RCフィルターの係数
     */
    private fun useRcFilter(currValues: FloatArray, prevValues: FloatArray, alpha: Float = 0.8f) {
        for (i in currValues.indices) {
            prevValues[i] = alpha * prevValues[i] + (1 - alpha) * currValues[i]
        }
    }

    private fun copyVector(src: FloatArray, dst: FloatArray) {
        System.arraycopy(src, 0, dst, 0, VECTOR_DIM)
    }

    companion object {
        private const val VECTOR_DIM = 3
        private const val MATRIX_DIM = 9
    }
}