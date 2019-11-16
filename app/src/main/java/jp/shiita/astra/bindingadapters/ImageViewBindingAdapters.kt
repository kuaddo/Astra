package jp.shiita.astra.bindingadapters

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import jp.shiita.astra.util.GlideApp

@BindingAdapter("url")
fun ImageView.bindImageUrl(url: String?) =
    url?.let { GlideApp.with(context).load(it).into(this) }

@BindingAdapter("uri")
fun ImageView.bindImageUrl(uri: Uri?) =
    uri?.let { GlideApp.with(context).load(it).into(this) }