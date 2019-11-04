package jp.shiita.astra.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import jp.shiita.astra.AstraApp
import jp.shiita.astra.di.module.ActivityModule
import jp.shiita.astra.di.module.ApiModule
import jp.shiita.astra.di.module.AppModule
import jp.shiita.astra.di.module.DataModule
import jp.shiita.astra.di.module.ViewModelDelegateModule
import jp.shiita.astra.di.module.ViewModelModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ActivityModule::class,
        ViewModelModule::class,
        ViewModelDelegateModule::class,
        ApiModule::class,
        DataModule::class,
        AppModule::class
    ]
)
interface AppComponent : AndroidInjector<AstraApp> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun applicationContext(applicationContext: Context): Builder

        fun build(): AppComponent
    }

    override fun inject(app: AstraApp)
}