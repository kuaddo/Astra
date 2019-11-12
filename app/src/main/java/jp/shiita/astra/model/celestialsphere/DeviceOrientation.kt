package jp.shiita.astra.model.celestialsphere

/**
 * 端末の方向（オイラー角）を格納するためのデータクラス
 * 詳しい説明は公式リファレンスを参照
 * @param azimuth [0, 2\pi)の角度(rad)
 * @param pitch [-\pi, \pi]の角度(rad)
 * @param roll [-\pi/2, \pi/2]の角度(rad)
 */
data class DeviceOrientation(
    val azimuth: Float,
    val pitch: Float,
    val roll: Float
)