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

data class BookTopic(
    val key: String,
    val name: String,
    val bookIds: List<Int>
)

object BookTopics {
    val all = listOf(
        BookTopic("tanakh", "Tanakh", listOf(9597, 9596, 9754, 14084, 9617, 14264, 14258, 14263, 14257, 14262, 14256, 14261, 14259, 14260, 14254)),
        BookTopic("mishnayot", "Mishnayot", listOf(37939, 37940, 37941, 37942, 37943, 37944, 37945, 37946, 37947, 37948, 37949, 37950, 37951)),
        BookTopic("gemara", "Gemara", listOf(37952, 37953, 37954, 37955, 37956, 37957, 37958, 37959, 37960, 37961, 37962, 37963, 37964, 37965, 37969, 37970, 37966, 37967, 37968, 37971, 40053)),
        BookTopic("rambam", "Mishneh Torah", listOf(58956, 58957, 58958, 58959, 58960, 58961, 58962, 58963, 58964, 58965, 58966, 58967, 58968)),
        BookTopic("tur", "Tur", listOf(14265, 14268, 14267, 14272, 14266, 14271, 14270)),
        BookTopic("shulchan-aruch", "Shulchan Aruch", listOf(14326, 14325, 14327, 9145, 9146, 9147, 14398, 40538, 40539)),
        BookTopic("mishnah-berurah", "Mishnah Berurah", listOf(60386, 60387, 60388, 60389, 60390, 60391))
    )

    fun find(key: String): BookTopic? = all.find { it.key == key }
}
