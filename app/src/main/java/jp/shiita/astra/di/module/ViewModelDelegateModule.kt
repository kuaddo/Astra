package jp.shiita.astra.di.module

import dagger.Binds
import dagger.Module
import jp.shiita.astra.delegate.LiveEventSnackbarViewModelDelegate
import jp.shiita.astra.delegate.LoadingViewModelDelegate
import jp.shiita.astra.delegate.MediatorLoadingViewModelDelegate
import jp.shiita.astra.delegate.SnackbarViewModelDelegate

@Suppress("unused")
@Module
abstract class ViewModelDelegateModule {

    @Binds
    abstract fun provideLoadingViewModelDelegate(delegate: MediatorLoadingViewModelDelegate): LoadingViewModelDelegate

    @Binds
    abstract fun provideSnackbarViewModelDelegate(delegate: LiveEventSnackbarViewModelDelegate): SnackbarViewModelDelegate
}