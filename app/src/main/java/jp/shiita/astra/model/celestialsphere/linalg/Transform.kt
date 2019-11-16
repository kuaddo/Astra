package jp.shiita.astra.model.celestialsphere.linalg

import android.location.Location
import jp.shiita.astra.model.celestialsphere.DeviceOrientation
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val DEG2RAD = PI / 180.0

fun getDeviceDirection(location: Location, orientation: DeviceOrientation): Vector3d {
    // 端末の初期方向を設定
    // 端末の画面の奥方向を端末の方向とする
    val initDirection = -createVector3dFromLocation(
        1.0,
        location
    )

    // 回転軸の設定
    // 極座標パラメータからではなく外積から
    // 末座標系の正規直交をワールド座標系に変換したものを直接求める
    val worldZAxis = Vector3d(0.0, 0.0, 1.0)
    val deviceZAxis = -initDirection.normalized
    val deviceXAxis = -(worldZAxis.cross(deviceZAxis))
    val deviceYAxis = deviceZAxis.cross(deviceXAxis)


    // 端末の向きで補正（z軸(方位角)に関しては画面の奥方向と関係ないため無視）
    // x軸の回転角
    val pitch = orientation.pitch.toDouble()
    // y軸の回転角
    val roll = orientation.roll.toDouble()
    val azimuth = orientation.azimuth.toDouble()
    val pitchRotated = rotateVector3dAroundAxis(
        initDirection,
        pitch,
        deviceXAxis
    )
    val rollRotated = rotateVector3dAroundAxis(
        pitchRotated,
        roll,
        deviceYAxis
    )
    return rotateVector3dAroundAxis(
        rollRotated,
        azimuth,
        deviceZAxis).normalized
}

/**
 * 極座標系から直交座標系3次元ベクトルを構成する関数
 */
fun createVector3dFromPolar(
    radius: Double,
    theta: Double,
    phi: Double
): Vector3d {
    val xyProjectedRadius = radius * sin(theta)
    val zProjectedRadius = radius * cos(theta)
    return Vector3d(
        xyProjectedRadius * cos(phi),
        xyProjectedRadius * sin(phi),
        zProjectedRadius
    )
}

fun createVector3dFromLocation(radius: Double, location: Location): Vector3d {
    val theta = PI / 2 - location.latitude * DEG2RAD
    val phi = location.longitude * DEG2RAD
    return createVector3dFromPolar(radius, theta, phi)
}

/**
 * ロドリゲスの回転公式を用いてtargetをaxis周りにradだけ回転
 * @param target 回転の対象となるベクトル
 * @param rad 回転角(rad)
 * @param axis 回転の軸となるベクトル
 * @return 回転後のベクトル
 */
private fun rotateVector3dAroundAxis(
    target: Vector3d,
    rad: Double,
    axis: Vector3d
): Vector3d {
    val halfRad = rad * 0.5
    // オイラーパラメータ
    val a = cos(halfRad)
    val b = sin(halfRad)
    // 補正された軸ベクトル
    val e = axis.normalized * b
    return target + e.cross(target) * (2 * a) + e.cross(e.cross(target)) * 2.0
}
