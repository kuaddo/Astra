package jp.shiita.astra.ui.call

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class CallModule {

    @ContributesAndroidInjector
    abstract fun contributeCallFragment(): CallFragment
}