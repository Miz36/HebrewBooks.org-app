package com.hebrewbooks.app.di

import android.content.Context
import com.hebrewbooks.app.repository.BookRepository
import com.hebrewbooks.app.service.ApiService
import com.hebrewbooks.app.service.DownloadService
import com.hebrewbooks.app.viewmodel.BookViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Services
    single { ApiService() }
    single { DownloadService(get<Context>()) }
    
    // Repository
    single { BookRepository(get(), get()) }
    
    // ViewModels
    viewModel { BookViewModel(get()) }
}
