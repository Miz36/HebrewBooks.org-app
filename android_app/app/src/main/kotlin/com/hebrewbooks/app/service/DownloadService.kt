package com.hebrewbooks.app.service

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.hebrewbooks.app.models.Book
import com.hebrewbooks.app.models.DownloadInfo
import com.hebrewbooks.app.models.DownloadStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class DownloadService(private val context: Context) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val _downloads = MutableStateFlow<List<DownloadInfo>>(emptyList())
    val downloads: StateFlow<List<DownloadInfo>> = _downloads.asStateFlow()

    private var downloadIdMap = mutableMapOf<Long, Int>()

    init {
        loadExistingDownloads()
    }

    fun downloadBook(book: Book): Long? {
        return try {
            if (_downloads.value.any { it.bookId == book.id && it.status == DownloadStatus.DOWNLOADING }) {
                return null
            }

            val request = DownloadManager.Request(Uri.parse(book.pdfUrl))
                .setTitle(book.title)
                .setDescription("Downloading ${book.title}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "HebrewBooks/${book.title.replace("[^a-zA-Z0-9]".toRegex(), "_")}_${book.id}.pdf"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)

            val downloadId = downloadManager.enqueue(request)
            downloadIdMap[downloadId] = book.id

            val newDownload = DownloadInfo(
                bookId = book.id,
                bookTitle = book.title,
                filePath = "",
                progress = 0,
                status = DownloadStatus.DOWNLOADING
            )
            _downloads.value = _downloads.value + newDownload

            downloadId
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun cancelDownload(bookId: Int): Boolean {
        val downloadId = downloadIdMap.entries.find { it.value == bookId }?.key
        
        if (downloadId != null) {
            downloadManager.remove(downloadId)
            downloadIdMap.remove(downloadId)
            _downloads.value = _downloads.value.filter { it.bookId != bookId }
            return true
        }
        return false
    }

    fun getDownloadInfo(bookId: Int): DownloadInfo? {
        return _downloads.value.find { it.bookId == bookId }
    }

    fun isDownloading(bookId: Int): Boolean {
        return _downloads.value.any { it.bookId == bookId && it.status == DownloadStatus.DOWNLOADING }
    }

    fun isDownloaded(bookId: Int): Boolean {
        return _downloads.value.any { it.bookId == bookId && it.status == DownloadStatus.COMPLETED }
    }

    fun getDownloadedFile(bookId: Int): File? {
        val downloadInfo = _downloads.value.find { it.bookId == bookId && it.status == DownloadStatus.COMPLETED }
        return downloadInfo?.filePath?.let { File(it) }?.takeIf { it.exists() }
    }

    fun openDownloadedFile(bookId: Int): Boolean {
        val file = getDownloadedFile(bookId) ?: return false
        return try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.fromFile(file), "application/pdf")
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun loadExistingDownloads() {
        try {
            val cursor = downloadManager.query(DownloadManager.Query())
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val downloadId = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                    val bookId = downloadIdMap[downloadId] ?: continue
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val localUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    val title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE)) ?: ""

                    val filePath = localUri?.let { Uri.parse(it).path }
                    val downloadStatus = when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.COMPLETED
                        DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                        DownloadManager.STATUS_PAUSED -> DownloadStatus.PENDING
                        DownloadManager.STATUS_PENDING -> DownloadStatus.PENDING
                        DownloadManager.STATUS_RUNNING -> DownloadStatus.DOWNLOADING
                        else -> DownloadStatus.PENDING
                    }

                    val downloadInfo = DownloadInfo(
                        bookId = bookId,
                        bookTitle = title,
                        filePath = filePath ?: "",
                        progress = if (downloadStatus == DownloadStatus.DOWNLOADING) 50 else 100,
                        status = downloadStatus
                    )
                    
                    if (_downloads.value.none { it.bookId == bookId }) {
                        _downloads.value = _downloads.value + downloadInfo
                    }
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cleanup() {
        downloadIdMap.clear()
    }
}
