package ru.crew.motley.piideo.chat.di

import android.app.Activity
import dagger.Binds
import dagger.Module
import dagger.android.ActivityKey
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import ru.crew.motley.piideo.chat.activity.ChatActivity

/**
 * Created by vas on 1/28/18.
 */
@Module(subcomponents = [ChatActivityComponent::class])
abstract class ChatActivityModule {
    @Binds
    @IntoMap
    @ActivityKey(ChatActivity::class)
    abstract fun bindChatActivityFactory(builder: ChatActivityComponent.Builder)
            : AndroidInjector.Factory<out Activity>
}
