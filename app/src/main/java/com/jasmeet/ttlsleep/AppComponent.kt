package com.jasmeet.ttlsleep

import com.jasmeet.ttlsleep.database.DatabaseModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class])
interface AppComponent {
    fun inject(mainActivity: MainActivity)
}
