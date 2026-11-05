package com.hebrewbooks.app

import android.app.Application
import android.app.ActivityManager
import com.hebrewbooks.app.di.appModule
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HebrewBooksApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val memoryCacheSize = if (activityManager.isLowRamDevice) {
            6L * 1024 * 1024
        } else {
            32L * 1024 * 1024
        }

        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder().maxSizeBytes(this@HebrewBooksApp, memoryCacheSize.toInt()).build()
            }
            .crossfade(false)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HebrewBooksApp)
            modules(appModule)
        }
    }
}
