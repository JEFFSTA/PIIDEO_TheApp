package ru.crew.motley.piideo.piideo.di

import dagger.Subcomponent
import dagger.android.AndroidInjector
import ru.crew.motley.piideo.piideo.fragment.PhotoImageFragment

/**
 * Created by vas on 1/28/18.
 */
@Subcomponent()
interface PhotoFragmentComponent : AndroidInjector<PhotoImageFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<PhotoImageFragment>()
}