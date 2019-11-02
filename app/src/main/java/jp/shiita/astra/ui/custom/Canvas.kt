package jp.shiita.astra.ui.custom

import android.content.Context
import android.util.AttributeSet
import io.skyway.Peer.Browser.Canvas

/**
 * SkyWayのパッケージに大文字が含まれている問題を解決するためだけのクラス
 */
class Canvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Canvas(context, attrs, defStyleAttr)