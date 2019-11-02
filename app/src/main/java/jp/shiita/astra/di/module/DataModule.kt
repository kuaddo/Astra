package jp.shiita.astra.di.module

import dagger.Binds
import dagger.Module
import jp.shiita.astra.data.PreferenceStorage
import jp.shiita.astra.data.SharedPreferenceStorage
import javax.inject.Singleton

@Module
abstract class DataModule {

    @Suppress("unused")
    @Binds
    @Singleton
    abstract fun providePreferenceStorage(preferenceStorage: SharedPreferenceStorage): PreferenceStorage
}