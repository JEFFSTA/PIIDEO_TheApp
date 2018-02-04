package ru.crew.motley.piideo.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import ru.crew.motley.piideo.Appp
import ru.crew.motley.piideo.chat.model.PiideoLoader
import javax.inject.Singleton

@Module()
class MainModule {

    @Provides
    @Singleton
    fun providesPiideoLoader(context: Context) = PiideoLoader(context)

    @Provides
    @Singleton
    fun provideContext(app: Application) : Context = app.applicationContext

}