package jp.shiita.astra.ui.waiting

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.shiita.astra.di.ViewModelKey

@Suppress("unused")
@Module
abstract class WaitingModule {

    @ContributesAndroidInjector
    abstract fun contributeWaitingFragment(): WaitingFragment

    @Binds
    @IntoMap
    @ViewModelKey(WaitingViewModel::class)
    abstract fun bindWaitingViewModel(viewModel: WaitingViewModel): ViewModel
}