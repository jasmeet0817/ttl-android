package com.jasmeet.ttlsleep.database

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule(private val context: Context) {
    @Provides
    @Singleton
    fun provideContext(): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideDatabaseSingleton(context: Context): DatabaseSingleton {
        return DatabaseSingleton.getInstance(context)
    }
}

