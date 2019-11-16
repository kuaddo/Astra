package jp.shiita.astra.extensions

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

fun Bitmap.toBytes(): ByteArray = ByteArrayOutputStream().let { stream ->
    compress(Bitmap.CompressFormat.JPEG, 80, stream)    // とりあえず80くらいにしておく
    stream.toByteArray()
}