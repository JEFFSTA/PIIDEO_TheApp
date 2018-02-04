package ru.crew.motley.piideo.piideo.di

import dagger.Subcomponent
import dagger.android.AndroidInjector
import ru.crew.motley.piideo.piideo.fragment.PhotoImageFragment

@Subcomponent()
interface PhotoFragmentComponent : AndroidInjector<PhotoImageFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<PhotoImageFragment>()
}