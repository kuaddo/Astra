package jp.shiita.astra.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.astra.receiver.HangUpReceiver

@Suppress("unused")
@Module
abstract class BroadcastReceiverModule {

    @ContributesAndroidInjector
    abstract fun contributeHangUpReceiver(): HangUpReceiver
}