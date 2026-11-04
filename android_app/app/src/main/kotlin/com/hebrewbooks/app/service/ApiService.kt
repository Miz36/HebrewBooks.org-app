package com.hebrewbooks.app.service

import com.hebrewbooks.app.models.Book
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

class ApiService {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Search for books by query
     * HebrewBooks doesn't have a public API, so we scrape the search results
     */
    suspend fun searchBooks(query: String, page: Int = 1): List<Book> {
        return try {
            val searchUrl = "https://hebrewbooks.org/search.aspx?k=$query&p=$page"
            val request = Request.Builder()
                .url(searchUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Search failed: ${response.code}")
                }
                
                val html = response.body?.string() ?: throw Exception("Empty response")
                parseSearchResults(html)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get book details by ID
     */
    suspend fun getBookDetails(bookId: Int): Book? {
        return try {
            val bookUrl = "https://hebrewbooks.org/$bookId"
            val request = Request.Builder()
                .url(bookUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to load book: ${response.code}")
                }
                
                val html = response.body?.string() ?: throw Exception("Empty response")
                parseBookDetails(html, bookId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get trending or featured books
     */
    suspend fun getFeaturedBooks(): List<Book> {
        return try {
            val request = Request.Builder()
                .url("https://hebrewbooks.org/")
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to load featured: ${response.code}")
                }
                
                val html = response.body?.string() ?: throw Exception("Empty response")
                parseFeaturedBooks(html)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseSearchResults(html: String): List<Book> {
        val books = mutableListOf<Book>()
        val doc = Jsoup.parse(html)
        
        // Find book result items - this selector may need adjustment
        val items = doc.select("div.book-item, a.book-link, table.tbllist tr")
        
        for (item in items) {
            try {
                val id = item.attr("href")?.let { extractBookId(it) } 
                    ?: item.select("a").firstOrNull()?.attr("href")?.let { extractBookId(it) }
                    ?: continue
                
                val title = item.select("a").firstOrNull()?.text()?.trim() ?: continue
                val author = item.select(".author, td:eq(1)").firstOrNull()?.text()?.trim() ?: "Unknown"
                
                books.add(Book(
                    id = id,
                    title = title,
                    author = author,
                    pages = 0,
                    thumbnailUrl = "https://hebrewbooks.org/thumbs/$id.png"
                ))
            } catch (e: Exception) {
                continue
            }
        }
        
        return books.take(50) // Limit results
    }

    private fun parseBookDetails(html: String, bookId: Int): Book {
        val doc = Jsoup.parse(html)
        
        val title = doc.select("#bookTitle, h1, .booktitle").firstOrNull()?.text()?.trim() 
            ?: "Untitled"
        
        val author = doc.select("#bookAuthor, .author, td:contains(Author:)").firstOrNull()?.text()?.trim() 
            ?: "Unknown"
        
        val pages = doc.select("td:contains(Pages:), #bookPages").firstOrNull()?.text()?.trim()
            ?.let { extractNumber(it) } ?: 0
        
        val year = doc.select("td:contains(Year:), #bookYear").firstOrNull()?.text()?.trim()
        val city = doc.select("td:contains(City:), #bookCity").firstOrNull()?.text()?.trim()
        
        return Book(
            id = bookId,
            title = title,
            author = author,
            pages = pages,
            year = year,
            city = city,
            thumbnailUrl = "https://hebrewbooks.org/thumbs/$bookId.png"
        )
    }

    private fun parseFeaturedBooks(html: String): List<Book> {
        val books = mutableListOf<Book>()
        val doc = Jsoup.parse(html)
        
        // Try to find featured/new books on homepage
        val items = doc.select("a[href~=/\\d+$], img[src~=/thumbs/\\d+\\.png]")
        
        for (item in items) {
            try {
                val href = item.attr("href")
                val id = extractBookId(href) ?: continue
                
                val title = item.attr("title")?.takeIf { it.isNotBlank() } 
                    ?: item.attr("alt")?.takeIf { it.isNotBlank() } 
                    ?: continue
                
                books.add(Book(
                    id = id,
                    title = title,
                    author = "Unknown",
                    pages = 0,
                    thumbnailUrl = "https://hebrewbooks.org/thumbs/$id.png"
                ))
            } catch (e: Exception) {
                continue
            }
        }
        
        return books.take(20)
    }

    private fun extractBookId(url: String): Int? {
        val regex = Regex("(\\d{1,6})")
        val match = regex.find(url) ?: return null
        return match.groupValues[1].toIntOrNull()
    }

    private fun extractNumber(text: String): Int {
        val regex = Regex("\\d+")
        val match = regex.find(text) ?: return 0
        return match.value.toIntOrNull() ?: 0
    }
}
