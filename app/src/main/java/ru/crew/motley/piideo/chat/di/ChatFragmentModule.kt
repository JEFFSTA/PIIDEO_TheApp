package ru.crew.motley.piideo.chat.di

import android.support.v4.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap
import ru.crew.motley.piideo.chat.fragment.ChatFragment

/**
 * Created by vas on 1/28/18.
 */
@Module(subcomponents = [ChatFragmentComponent::class])
abstract class ChatFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(ChatFragment::class)
    abstract fun bindChatFragmentFactory(builder: ChatFragmentComponent.Builder)
            : AndroidInjector.Factory<out Fragment>
}