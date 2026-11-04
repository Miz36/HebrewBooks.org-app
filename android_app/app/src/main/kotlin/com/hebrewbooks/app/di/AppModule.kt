package com.hebrewbooks.app.di

import android.content.Context
import com.hebrewbooks.app.repository.BookRepository
import com.hebrewbooks.app.service.ApiService
import com.hebrewbooks.app.service.DownloadService
import com.hebrewbooks.app.viewmodel.BookViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { androidContext() }
    
    single { ApiService() }
    single { DownloadService(androidContext()) }
    
    single { BookRepository(get(), get()) }
    
    viewModel { BookViewModel(get()) }
}
