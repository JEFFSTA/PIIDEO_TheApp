package ru.crew.motley.piideo.chat.di

import dagger.Subcomponent
import dagger.android.AndroidInjector
import ru.crew.motley.piideo.chat.activity.ChatActivity

/**
 * Created by vas on 1/28/18.
 */
@Subcomponent(modules = [ChatFragmentModule::class])
public interface ChatActivityComponent : AndroidInjector<ChatActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ChatActivity>()
}