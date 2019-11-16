package jp.shiita.astra.extensions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import jp.shiita.astra.R
import jp.shiita.astra.di.AssistedViewModelFactory
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