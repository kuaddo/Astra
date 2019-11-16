package jp.shiita.astra.di.module

import dagger.Module
import dagger.Provides
import jp.shiita.astra.delegate.LiveEventSnackbarViewModelDelegate
import jp.shiita.astra.delegate.LoadingViewModelDelegate
import jp.shiita.astra.delegate.MediatorLoadingViewModelDelegate
import jp.shiita.astra.delegate.SnackbarViewModelDelegate

@Suppress("unused")
@Module
abstract class ViewModelDelegateModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideLoadingViewModelDelegate(): LoadingViewModelDelegate =
            MediatorLoadingViewModelDelegate()

        @Provides
        @JvmStatic
        fun provideSnackbarViewModelDelegate(): SnackbarViewModelDelegate =
            LiveEventSnackbarViewModelDelegate()
    }
}