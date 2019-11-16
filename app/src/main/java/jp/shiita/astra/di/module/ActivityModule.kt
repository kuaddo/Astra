package jp.shiita.astra.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.astra.di.ActivityScoped
import jp.shiita.astra.ui.MainActivity
import jp.shiita.astra.ui.call.CallModule
import jp.shiita.astra.ui.images.ImagesModule
import jp.shiita.astra.ui.waiting.WaitingModule

@Suppress("unused")
@Module
abstract class ActivityModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = [WaitingModule::class, CallModule::class, ImagesModule::class])
    abstract fun contributeMainActivity(): MainActivity
}