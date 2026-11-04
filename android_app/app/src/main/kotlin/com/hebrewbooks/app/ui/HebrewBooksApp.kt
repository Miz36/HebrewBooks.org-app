package com.hebrewbooks.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hebrewbooks.app.ui.screens.BookDetailScreen
import com.hebrewbooks.app.ui.screens.DownloadsScreen
import com.hebrewbooks.app.ui.screens.HomeScreen
import com.hebrewbooks.app.ui.screens.PdfViewerScreen
import com.hebrewbooks.app.ui.screens.SearchScreen

@Composable
fun HebrewBooksApp() {
    val navController = rememberNavController()
    
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(
                    onSearchClick = { navController.navigate("search") },
                    onBookClick = { book ->
                        navController.navigate("bookDetail/${book.id}")
                    },
                    onDownloadsClick = { navController.navigate("downloads") }
                )
            }
            
            composable("search") {
                SearchScreen(
                    onBackClick = { navController.popBackStack() },
                    onBookClick = { book ->
                        navController.navigate("bookDetail/${book.id}")
                    }
                )
            }
            
            composable("bookDetail/{bookId}") { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId")?.toIntOrNull() ?: 0
                BookDetailScreen(
                    bookId = bookId,
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            composable("downloads") {
                DownloadsScreen(
                    onBackClick = { navController.popBackStack() },
                    onBookClick = { book ->
                        navController.navigate("bookDetail/${book.id}")
                    },
                    onOpenPdf = { bookId ->
                        navController.navigate("pdfViewer/$bookId")
                    }
                )
            }
            
            composable("pdfViewer/{bookId}") { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId")?.toIntOrNull() ?: 0
                PdfViewerScreen(
                    bookId = bookId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
