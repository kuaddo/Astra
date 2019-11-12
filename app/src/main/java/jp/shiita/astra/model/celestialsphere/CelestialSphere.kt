package jp.shiita.astra.model.celestialsphere

import android.location.Location
import jp.shiita.astra.model.celestialsphere.linalg.Vector3d
import jp.shiita.astra.model.celestialsphere.linalg.createVector3dFromLocation
import jp.shiita.astra.model.celestialsphere.linalg.getDeviceDirection
import timber.log.Timber
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

private const val EARTH_RADIUS = 6400.0
private const val CELESTIAL_SPHERE_RADIUS = EARTH_RADIUS * 10
/**
 * 方向ベクトルの係数kを求めるための定数
 */
private const val RADIUS_FACTOR = (CELESTIAL_SPHERE_RADIUS + EARTH_RADIUS) *
        (CELESTIAL_SPHERE_RADIUS - EARTH_RADIUS)

/**
 * 経度(degree)の限界値
 */
private const val PHI_LIMIT = 360
/**
 * 緯度(degree)の限界値
 */
private const val THETA_LIMIT = 180

private const val DELTA_PHI = 30
private const val DELTA_THETA = 15

private const val RAD2DEG = 180 / PI

/**
 * 仮想天球を表すクラス
 */
class CelestialSphere(
    private val deltaTheta: Int = DELTA_THETA,
    private val deltaPhi: Int = DELTA_PHI
) {

    /**
     * 天球上のグリッドを表すデータクラス
     * @param phiGridNum 経度方向のグリッド番号
     * @param thetaGridNum 緯度方向のグリッド番号
     */
    data class CelestialGrid(val phiGridNum: Int, val thetaGridNum: Int)


    /**
     * 経度方向のグリッド数
     */
    val nPhiGrid: Int = PHI_LIMIT / deltaPhi
    /**
     * 緯度方向のグリッド数
     */
    val nThetaGrid: Int = THETA_LIMIT / deltaTheta

    /**
     * 経度方向のグリッド番号を格納する配列
     */
    private val phiGrids = IntArray(nPhiGrid)
    /**
     * 緯度方向のグリッド番号を格納する配列
     */
    private val thetaGrids = IntArray(nThetaGrid)

    init {
        for (i in 0 until phiGrids.size) {
            phiGrids[i] = -PHI_LIMIT / 2 + i * deltaPhi
        }

        for (i in 0 until thetaGrids.size) {
            thetaGrids[i] = i * deltaTheta
        }
    }

    /**
     * 端末の方向ベクトルと地球上の位置から天球のグリッドを算出
     */
    fun searchGrid(
        location: Location,
        deviceOrientation: DeviceOrientation
    ): CelestialGrid {
        val plotVector = plotCelestialSphere(location, deviceOrientation).normalized

        val phi = plotVector.phi * RAD2DEG
        val theta = plotVector.theta * RAD2DEG
        val phiGridNum = binarySearch(phiGrids, deltaPhi, phi)
        val thetaGridNum = binarySearch(thetaGrids, deltaTheta, theta)
        return CelestialGrid(phiGridNum, thetaGridNum)
    }

    /**
     * 緯度経度と端末の方向から天球上のベクトルpを算出
     * 算出する過程を以下に示す
     * 地球上の点を表すベクトルqは以下の式で求まる
     *  q = (r cos\theta cos\phi, r cos\theta sin\phi, r sin\theta)
     * ただし、\thetaと\phiはそれぞれ経度と緯度を表し、rは地球の半径を表す
     * また、端末の単位方向ベクトルdと任意のk(>0)を導入するとpは以下のようにあらわせる
     *  p = q + kd
     * 天球上の点を表す式|p| = Rに代入してkの値を以下で定める
     *  k = - q \dot d + sqrt{(q \dot d)^2 + R^2 - r^2}
     * ただし、Rは天球の半径、q \dot dは内積計算を表す
     */
    fun plotCelestialSphere(
        location: Location,
        deviceOrientation: DeviceOrientation
    ): Vector3d {
        // 地球上の位置を算出
        val positionOnEarth = createVector3dFromLocation(EARTH_RADIUS, location)

        // 方向ベクトルを取得
        val direction = getDeviceDirection(location, deviceOrientation)

        val tempDot = positionOnEarth.dot(direction)
        val celestialFactor = -tempDot + sqrt(tempDot.pow(2) + RADIUS_FACTOR)
        return positionOnEarth + direction * celestialFactor
    }
}

/**
 * 二分探索
 */
private fun binarySearch(array: IntArray, delta: Int, target: Double): Int {
    val tag = "binarySearch"

    var lower = 0
    var upper = array.size
    var pivot = (lower + upper / 2)
    var found = false
    while (!found) {
        Timber.d(tag, "pivot = $pivot")
        val startValue = array[pivot]
        val endValue = startValue + delta
        if (startValue <= target && endValue > target) {
            found = true
        } else {
            if (startValue > target) {
                upper = pivot
            } else {
                lower = pivot
            }
            pivot = (lower + upper) / 2
        }
    }
    return pivot
}