package ru.crew.motley.piideo.fcm.di

import dagger.Subcomponent
import dagger.android.AndroidInjector
import ru.crew.motley.piideo.fcm.MessagingService

/**
 * Created by vas on 1/29/18.
 */
@Subcomponent
interface MessagingServiceComponent : AndroidInjector<MessagingService> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<MessagingService>()
}