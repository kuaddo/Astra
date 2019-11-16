package jp.shiita.astra.ui.images

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.shiita.astra.di.ViewModelKey

@Suppress("unused")
@Module
abstract class ImagesModule {

    @ContributesAndroidInjector
    abstract fun contributeSelectImagesFragment(): SelectImagesFragment

    @ContributesAndroidInjector
    abstract fun contributeViewImagesFragment(): ViewImagesFragment

    @Binds
    @IntoMap
    @ViewModelKey(SelectImagesViewModel::class)
    abstract fun bindSelectImagesViewModel(viewModel: SelectImagesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewImagesViewModel::class)
    abstract fun bindViewImagesViewModel(viewModel: ViewImagesViewModel): ViewModel
}