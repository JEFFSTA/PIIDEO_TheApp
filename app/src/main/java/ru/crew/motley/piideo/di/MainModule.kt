package ru.crew.motley.piideo.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import ru.crew.motley.piideo.Appp
import ru.crew.motley.piideo.chat.model.PiideoLoader
import javax.inject.Singleton

/**
 * Created by vas on 1/28/18.
 */
@Module()
class MainModule {

    @Provides
    @Singleton
    fun providesPiideoLoader(context: Context) = PiideoLoader(context)

    @Provides
    @Singleton
    fun provideContext(app: Application) : Context = app.applicationContext

}