package ru.crew.motley.piideo.piideo.di

import android.support.v4.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap
import ru.crew.motley.piideo.piideo.fragment.PhotoImageFragment

/**
 * Created by vas on 1/28/18.
 */
@Module(subcomponents = [PhotoFragmentComponent::class])
abstract class PhotoFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(PhotoImageFragment::class)
    abstract fun bindPhotoImageFragmentFactory(builder: PhotoFragmentComponent.Builder)
            : AndroidInjector.Factory<out Fragment>

}