package jp.shiita.astra.bindingadapters

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import jp.shiita.astra.util.GlideApp

@BindingAdapter("url")
fun ImageView.bindImageUrl(url: String?) = GlideApp.with(context).load(url).into(this)