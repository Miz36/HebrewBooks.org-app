package com.hebrewbooks.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hebrewbooks.app.models.Book
import com.hebrewbooks.app.models.BookSearchField
import com.hebrewbooks.app.models.BookTopic
import com.hebrewbooks.app.models.DownloadInfo
import com.hebrewbooks.app.models.DownloadStatus
import com.hebrewbooks.app.repository.BookRepository
import com.hebrewbooks.app.repository.BookUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookViewModel(private val repository: BookRepository) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchField = MutableStateFlow(BookSearchField.ANY_FIELD)
    val searchField: StateFlow<BookSearchField> = _searchField.asStateFlow()

    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _showDetails = MutableStateFlow(false)
    val showDetails: StateFlow<Boolean> = _showDetails.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<Int, Int>> = _downloadProgress.asStateFlow()

    private val _topicBooks = MutableStateFlow<List<Book>>(emptyList())
    val topicBooks: StateFlow<List<Book>> = _topicBooks.asStateFlow()

    private val _isLoadingTopic = MutableStateFlow(false)
    val isLoadingTopic: StateFlow<Boolean> = _isLoadingTopic.asStateFlow()

    // Main UI state
    val uiState: StateFlow<BookUiState> = repository.uiState
        .combine(_isSearching) { state, searching ->
            state.copy(isLoading = state.isLoading || searching)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookUiState())

    init {
        loadFeaturedBooks()
        viewModelScope.launch {
            repository.downloads.collectLatest { downloads ->
                _downloadProgress.value = downloads.associate { 
                    it.bookId to it.progress 
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        val field = _searchField.value
        
        // Debounce search
        viewModelScope.launch {
            _isSearching.value = true
            delay(300) // 300ms debounce
            if (_searchQuery.value == query && _searchField.value == field) {
                repository.searchBooks(query, field)
            }
            _isSearching.value = false
        }
    }

    fun onSearchFieldChanged(field: BookSearchField) {
        if (_searchField.value != field) {
            _searchField.value = field
            onSearchQueryChanged(_searchQuery.value)
        }
    }

    fun searchBooks(query: String, field: BookSearchField = _searchField.value) {
        viewModelScope.launch {
            repository.searchBooks(query, field)
        }
    }

    fun loadFeaturedBooks() {
        viewModelScope.launch {
            repository.loadFeaturedBooks()
        }
    }

    fun selectBook(book: Book) {
        _selectedBook.value = book
        _showDetails.value = true
    }

    fun clearSelection() {
        _selectedBook.value = null
        _showDetails.value = false
    }

    fun downloadBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.downloadBook(book)
        }
    }

    fun cancelDownload(bookId: Int) {
        repository.cancelDownload(bookId)
    }

    fun isDownloading(bookId: Int): Boolean {
        return repository.isDownloading(bookId)
    }

    fun isDownloaded(bookId: Int): Boolean {
        return repository.isDownloaded(bookId)
    }

    fun openDownloadedFile(bookId: Int): Boolean {
        return repository.openDownloadedFile(bookId)
    }

    suspend fun getBookDetails(bookId: Int): Book? = repository.getBookDetails(bookId)

    fun getDownloadedFile(bookId: Int) = repository.getDownloadedFile(bookId)

    fun loadTopic(topic: BookTopic) {
        viewModelScope.launch {
            _isLoadingTopic.value = true
            _topicBooks.value = emptyList()
            try {
                _topicBooks.value = repository.getBooksByIds(topic.bookIds)
            } finally {
                _isLoadingTopic.value = false
            }
        }
    }

    fun getDownloadProgress(bookId: Int): Int {
        return _downloadProgress.value[bookId] ?: 0
    }

    fun refresh() {
        viewModelScope.launch {
            if (_searchQuery.value.isBlank()) {
                repository.loadFeaturedBooks()
            } else {
                repository.searchBooks(_searchQuery.value)
            }
        }
    }
}
