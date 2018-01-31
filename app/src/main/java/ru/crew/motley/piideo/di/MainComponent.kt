package ru.crew.motley.piideo.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import ru.crew.motley.piideo.Appp
import ru.crew.motley.piideo.chat.di.ChatActivityModule
import ru.crew.motley.piideo.fcm.di.MessagingServiceModule
import ru.crew.motley.piideo.piideo.di.PiideoActivityModule
import javax.inject.Singleton

@Component(modules = [
    AndroidSupportInjectionModule::class,
    ChatActivityModule::class,
    PiideoActivityModule::class,
    MessagingServiceModule::class,
    MainModule::class])
@Singleton
interface MainComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): MainComponent
    }

    fun inject(app: Appp)
}