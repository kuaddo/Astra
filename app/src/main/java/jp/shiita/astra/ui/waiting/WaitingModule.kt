package jp.shiita.astra.ui.waiting

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class WaitingModule {

    @ContributesAndroidInjector
    abstract fun contributeWaitingFragment(): WaitingFragment
}