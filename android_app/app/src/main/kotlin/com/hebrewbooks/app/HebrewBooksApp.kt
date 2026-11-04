package com.hebrewbooks.app

import android.app.Application
import com.hebrewbooks.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HebrewBooksApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HebrewBooksApp)
            modules(appModule)
        }
    }
}
