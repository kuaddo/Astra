package jp.shiita.astra.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.astra.di.ActivityScoped
import jp.shiita.astra.ui.MainActivity

@Suppress("unused")
@Module
abstract class ActivityModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = [])
    internal abstract fun contributeMainActivity(): MainActivity
}