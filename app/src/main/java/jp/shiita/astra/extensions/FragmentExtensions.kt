package jp.shiita.astra.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.google.android.material.snackbar.Snackbar
import jp.shiita.astra.R
import jp.shiita.astra.di.AssistedViewModelFactory
import jp.shiita.astra.util.SnackbarMessage
import jp.shiita.astra.util.SnackbarMessageRes
import jp.shiita.astra.util.SnackbarMessageResParams
import jp.shiita.astra.util.SnackbarMessageText
import jp.shiita.astra.util.ToastMessage
import jp.shiita.astra.util.ToastMessageRes
import jp.shiita.astra.util.ToastMessageResParams
import jp.shiita.astra.util.ToastMessageText
import permissions.dispatcher.PermissionRequest

/**
 * bindingをnullにするLifecycleObserverの登録と、lifecycleOwnerセットをする
 */
fun <T : ViewDataBinding> Fragment.dataBinding(@LayoutRes layoutResId: Int): Lazy<T> {
    return object : Lazy<T> {

        private var binding: T? = null

        override fun isInitialized(): Boolean = binding != null

        override val value: T
            get() = binding ?: DataBindingUtil.inflate<T>(
                layoutInflater,
                layoutResId,
                requireActivity().findViewById(id) as? ViewGroup,
                false
            ).also {
                binding = it
                it.lifecycleOwner = viewLifecycleOwner
                viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    @Suppress("unused")
                    fun onDestroyView() {
                        viewLifecycleOwner.lifecycle.removeObserver(this)
                        binding = null // For Fragment's view recreation
                    }
                })
            }
    }
}

fun Fragment.setupSnackbar(snackbarEvent: LiveData<SnackbarMessage>) {
    snackbarEvent.observeNonNull(viewLifecycleOwner) { message ->
        when (message) {
            is SnackbarMessageRes -> view?.snackbar(message.resId, message.duration)
            is SnackbarMessageResParams -> view?.snackbar(
                message.resId,
                message.params,
                message.duration
            )
            is SnackbarMessageText -> view?.snackbar(message.text, message.duration)
        }
    }
}

fun Fragment.setupToast(toastEvent: LiveData<ToastMessage>) {
    toastEvent.observeNonNull(viewLifecycleOwner) { message ->
        when (message) {
            is ToastMessageRes -> context?.toast(message.resId, message.duration)
            is ToastMessageResParams -> context?.toast(
                message.resId,
                message.params,
                message.duration
            )
            is ToastMessageText -> context?.toast(message.text, message.duration)
        }
    }
}

inline fun <reified VM : ViewModel> Fragment.assistedViewModels(crossinline create: () -> VM): Lazy<VM> =
    viewModels { AssistedViewModelFactory { create() } }

inline fun <reified VM : ViewModel> Fragment.assistedActivityViewModels(crossinline create: () -> VM): Lazy<VM> =
    activityViewModels { AssistedViewModelFactory { create() } }

fun Fragment.showRationaleForContactsDialog(
    request: PermissionRequest,
    @StringRes titleRes: Int,
    @StringRes messageRes: Int
) = MaterialDialog(requireContext()).show {
    title(titleRes)
    message(messageRes)
    positiveButton(R.string.ok) { request.proceed() }
    cancelable(false)
}

fun Fragment.showOnContactsDeniedDialog(@StringRes messageRes: Int) =
    MaterialDialog(requireContext()).show {
        message(messageRes)
        positiveButton(R.string.ok) { findNavController().popBackStack() }
        cancelable(false)
    }

fun Fragment.showOnContactsNeverAskAgainDialog(@StringRes messageRes: Int) =
    MaterialDialog(requireContext()).show {
        message(messageRes)
        positiveButton(R.string.ok) { startAppSettingActivity() }
        negativeButton(R.string.back)
        onDismiss { findNavController().popBackStack() }
        cancelable(false)
    }

private fun Fragment.startAppSettingActivity() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:${requireContext().packageName}")
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

private fun View.snackbar(text: String, duration: Int) =
    Snackbar.make(this, text, duration).show()

private fun View.snackbar(@StringRes resId: Int, duration: Int) =
    Snackbar.make(this, resId, duration).show()

private fun View.snackbar(@StringRes resId: Int, params: List<String>, duration: Int) =
    Snackbar.make(this, context.getString(resId, *params.toTypedArray()), duration).show()

private fun Context.toast(text: String, duration: Int) =
    Toast.makeText(this, text, duration).show()

private fun Context.toast(@StringRes resId: Int, duration: Int) =
    Toast.makeText(this, resId, duration).show()

private fun Context.toast(@StringRes resId: Int, params: List<String>, duration: Int) =
    Toast.makeText(this, getString(resId, *params.toTypedArray()), duration).show()