package jp.shiita.astra.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AssistedViewModelFactory<T : ViewModel>(
    private val create: () -> T
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel?> create(modelClass: Class<VM>): VM {
        return create() as VM
    }
}