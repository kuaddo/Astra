package jp.shiita.astra.model.celestialsphere.linalg

import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

data class Vector3d(val x: Double, val y: Double, val z: Double) {

    val length: Double get() = sqrt(this.dot(this))

    /**
     * 正規化されたベクトル
     */
    val normalized: Vector3d get() = this / length

    /**
     * [0, \pi]で仰角(rad)を返す
     */
    val theta: Double get() = acos(z / length)

    /**
     * [-\pi, \pi]で方位角(rad)を返す
     */
    val phi: Double get() = atan2(y, x)

    /**
     * 内積計算を行うメソッド
     */
    fun dot(other: Vector3d): Double = x * other.x + y * other.y + z * other.z

    /**
     * 外積計算を行うメソッド
     */
    fun cross(other: Vector3d): Vector3d {
        val newX = y * other.z - z * other.y
        val newY = z * other.x - x * other.z
        val newZ = x * other.y - y * other.x
        return Vector3d(newX, newY, newZ)
    }

    /**
     * ベクトルの反転操作
     */
    operator fun unaryMinus(): Vector3d =
        Vector3d(-x, -y, -z)

    /**
     * ベクトルの合成操作
     */
    operator fun plus(other: Vector3d): Vector3d {
        return Vector3d(
            x + other.x,
            y + other.y,
            z + other.z
        )
    }

    operator fun minus(other: Vector3d): Vector3d = this + (-other)

    operator fun times(scalar: Double): Vector3d {
        return Vector3d(
            x * scalar,
            y * scalar,
            z * scalar
        )
    }

    operator fun div(scalar: Double): Vector3d {
        return Vector3d(
            x / scalar,
            y / scalar,
            z / scalar
        )
    }
}


