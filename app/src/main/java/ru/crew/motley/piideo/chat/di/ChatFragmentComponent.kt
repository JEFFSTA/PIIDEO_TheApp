package ru.crew.motley.piideo.chat.di

import dagger.Subcomponent
import dagger.android.AndroidInjector
import ru.crew.motley.piideo.chat.fragment.ChatFragment

/**
 * Created by vas on 1/28/18.
 */
@Subcomponent()
interface ChatFragmentComponent : AndroidInjector<ChatFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ChatFragment>()
}