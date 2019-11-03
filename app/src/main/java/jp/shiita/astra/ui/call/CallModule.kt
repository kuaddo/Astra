package jp.shiita.astra.ui.call

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.shiita.astra.di.ViewModelKey

@Suppress("unused")
@Module
abstract class CallModule {

    @ContributesAndroidInjector
    abstract fun contributeCallFragment(): CallFragment

    @Binds
    @IntoMap
    @ViewModelKey(CallViewModel::class)
    abstract fun bindCallViewModel(viewModel: CallViewModel): ViewModel
}