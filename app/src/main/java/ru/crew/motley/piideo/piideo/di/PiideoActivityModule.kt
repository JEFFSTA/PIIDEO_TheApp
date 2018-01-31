package ru.crew.motley.piideo.piideo.di

import android.app.Activity
import dagger.Binds
import dagger.Module
import dagger.android.ActivityKey
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import ru.crew.motley.piideo.piideo.activity.PiideoActivity

/**
 * Created by vas on 1/28/18.
 */
@Module(subcomponents = [PiideoActivityComponent::class])
abstract class PiideoActivityModule {
    @Binds
    @IntoMap
    @ActivityKey(PiideoActivity::class)
    abstract fun bindPiideoActivityFactory(builder: PiideoActivityComponent.Builder)
            : AndroidInjector.Factory<out Activity>
}