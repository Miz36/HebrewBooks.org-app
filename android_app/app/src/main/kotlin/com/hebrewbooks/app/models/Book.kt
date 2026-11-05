package com.hebrewbooks.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val pages: Int,
    val year: String? = null,
    val city: String? = null,
    val thumbnailUrl: String? = null,
    val pdfUrl: String = "https://hebrewbooks.org/pdf/$id.pdf"
) : Parcelable {
    val displayYear: String
        get() = year?.takeIf { it.isNotBlank() } ?: "Unknown"
    
    val displayCity: String
        get() = city?.takeIf { it.isNotBlank() } ?: "Unknown"
}

@Parcelize
data class SearchResult(
    val books: List<Book>,
    val total: Int,
    val query: String
) : Parcelable

@Parcelize
data class DownloadInfo(
    val bookId: Int,
    val bookTitle: String,
    val filePath: String,
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.PENDING
) : Parcelable

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class BookSearchField(val label: String) {
    ANY_FIELD("Any field"),
    NAME("Name"),
    AUTHOR("Author")
}
