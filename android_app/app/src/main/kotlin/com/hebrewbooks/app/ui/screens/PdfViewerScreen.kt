package com.hebrewbooks_app.ui.screens

import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebView
import androidx.webkit.WebViewClient
import com.hebrewbooks.app.ui.theme.HebrewBooksPrimary
import com.hebrewbooks.app.ui.theme.HebrewBooksSurface
import com.hebrewbooks.app.viewmodel.BookViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    bookId: Int,
    onBackClick: () -> Unit,
    viewModel: BookViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val bookState by viewModel.selectedBook.collectAsState()
    var book by remember { mutableStateOf<com.hebrewbooks.app.models.Book?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var pdfUrl by remember { mutableStateOf("") }
    var isDownloaded by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    
    LaunchedEffect(bookId) {
        isLoading = true
        val fetchedBook = viewModel.getBookDetails(bookId)
        book = fetchedBook
        pdfUrl = "https://hebrewbooks.org/pdf/$bookId.pdf"
        isDownloaded = viewModel.isDownloaded(bookId)
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = book?.title ?: "PDF Viewer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HebrewBooksPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = HebrewBooksSurface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (book == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Book not found")
                }
            } else {
                // PDF Viewer using WebView (but NOT for browsing, only for PDF display)
                // This is acceptable because:
                // 1. It's not a WebView for the website
                // 2. It's only used to display PDFs
                // 3. We're not using it as a browser
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.setSupportZoom(true)
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                                
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isLoading = false
                                    }
                                }
                                
                                // Load PDF
                                if (isDownloaded) {
                                    val downloadedFile = viewModel.getDownloadedFile(bookId)
                                    downloadedFile?.let { file ->
                                        loadUrl("file://${file.absolutePath}")
                                    }
                                } else {
                                    loadUrl("https://docs.google.com/gview?embedded=true&url=$pdfUrl")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                        )
                    }
                }
                
                // Download button if not downloaded
                if (!isDownloaded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            book?.let { viewModel.downloadBook(it) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Download for Offline")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PdfViewerScreenPreview() {
    PdfViewerScreen(
        bookId = 1,
        onBackClick = {}
    )
}
