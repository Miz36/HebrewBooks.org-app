package com.hebrewbooks.app.repository

import com.hebrewbooks.app.models.Book
import com.hebrewbooks.app.models.BookSearchField
import com.hebrewbooks.app.models.DownloadInfo
import com.hebrewbooks.app.models.DownloadStatus
import com.hebrewbooks.app.service.ApiService
import com.hebrewbooks.app.service.DownloadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class BookRepository(
    private val apiService: ApiService,
    private val downloadService: DownloadService
) {
    private val _searchResults = MutableStateFlow<List<Book>>(emptyList())
    val searchResults: StateFlow<List<Book>> = _searchResults.asStateFlow()

    private val _featuredBooks = MutableStateFlow<List<Book>>(emptyList())
    val featuredBooks: StateFlow<List<Book>> = _featuredBooks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val downloads: StateFlow<List<DownloadInfo>> = downloadService.downloads

    // Combined state for UI
    val uiState: Flow<BookUiState> = combine(
        combine(_searchResults, _featuredBooks, _isLoading, _error, _searchQuery) {
                searchResults, featured, loading, error, query ->
            BookUiState(
                searchResults = searchResults,
                featuredBooks = featured,
                isLoading = loading,
                error = error,
                searchQuery = query
            )
        },
        downloads
    ) { state, currentDownloads ->
        state.copy(downloads = currentDownloads)
    }

    suspend fun searchBooks(query: String, field: BookSearchField = BookSearchField.ANY_FIELD) = withContext(Dispatchers.IO) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return@withContext
        }

        _isLoading.value = true
        _error.value = null

        try {
            val results = apiService.searchBooks(query)
            _searchResults.value = when (field) {
                BookSearchField.ANY_FIELD -> results
                BookSearchField.NAME -> results.filter { it.title.contains(query, ignoreCase = true) }
                BookSearchField.AUTHOR -> results.filter { it.author.contains(query, ignoreCase = true) }
            }
        } catch (e: Exception) {
            _error.value = e.message ?: "Search failed"
            _searchResults.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun loadFeaturedBooks() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            val books = apiService.getFeaturedBooks()
            _featuredBooks.value = books
        } catch (e: Exception) {
            _error.value = e.message ?: "Failed to load featured books"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun getBookDetails(bookId: Int): Book? = withContext(Dispatchers.IO) {
        return@withContext apiService.getBookDetails(bookId)
    }

    fun downloadBook(book: Book): Long? {
        return downloadService.downloadBook(book)
    }

    fun cancelDownload(bookId: Int): Boolean {
        return downloadService.cancelDownload(bookId)
    }

    fun isDownloading(bookId: Int): Boolean {
        return downloadService.isDownloading(bookId)
    }

    fun isDownloaded(bookId: Int): Boolean {
        return downloadService.isDownloaded(bookId)
    }

    fun getDownloadedFile(bookId: Int) = downloadService.getDownloadedFile(bookId)

    fun openDownloadedFile(bookId: Int): Boolean {
        return downloadService.openDownloadedFile(bookId)
    }

    fun getDownloadInfo(bookId: Int): DownloadInfo? {
        return downloadService.getDownloadInfo(bookId)
    }
}

data class BookUiState(
    val searchResults: List<Book> = emptyList(),
    val featuredBooks: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val downloads: List<DownloadInfo> = emptyList()
)
