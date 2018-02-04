package ru.crew.motley.piideo.fcm.di

import android.app.Service
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ServiceKey
import dagger.multibindings.IntoMap
import ru.crew.motley.piideo.fcm.MessagingService

@Module(subcomponents = [MessagingServiceComponent::class])
abstract class MessagingServiceModule {
    @Binds
    @IntoMap
    @ServiceKey(MessagingService::class)
    abstract fun bindMessagingServiceFactory(builder: MessagingServiceComponent.Builder)
            : AndroidInjector.Factory<out Service>

}