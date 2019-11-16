package jp.shiita.astra.ui.images

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@AssistedModule
@Module(includes = [AssistedInject_ImagesModule::class])
abstract class ImagesModule {

    @ContributesAndroidInjector
    abstract fun contributeSelectImagesFragment(): SelectImagesFragment

    @ContributesAndroidInjector
    abstract fun contributeViewImagesFragment(): ViewImagesFragment
}