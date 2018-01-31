package ru.crew.motley.piideo.piideo.di

import dagger.Subcomponent
import dagger.android.AndroidInjector
import ru.crew.motley.piideo.piideo.activity.PiideoActivity

/**
 * Created by vas on 1/28/18.
 */
@Subcomponent(modules = [PhotoFragmentModule::class])
interface PiideoActivityComponent : AndroidInjector<PiideoActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<PiideoActivity>()
}