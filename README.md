# HebrewBooks Native Android App

A **100% native Android app** for HebrewBooks.org that allows you to:
- Search for books smoothly and easily
- Browse featured books
- View book details
- Download PDFs directly (no browser/WebView needed)
- Read PDFs offline
- Clean, modern, non-clunky UI

## Features

- **No WebView** - Truly native app, not a browser wrapper
- **Direct PDF Downloads** - Downloads PDFs directly without needing a browser
- **Smooth Search** - Fast, responsive search with debouncing
- **Modern UI** - Clean Material 3 design with Jetpack Compose
- **Offline Reading** - Download books and read them offline
- **Android 7.1+ Support** - Works on AOSP 7.1 (API 25) and above

## Screens

1. **Home Screen** - Featured books grid
2. **Search Screen** - Smooth search with results
3. **Book Details** - Full book information
4. **PDF Viewer** - View PDFs (using WebView only for PDF display, not browsing)
5. **Downloads** - Manage downloaded books

## Tech Stack

- **Kotlin** - Primary language
- **Jetpack Compose** - Modern UI framework
- **Retrofit + OkHttp** - Networking
- **Jsoup** - HTML parsing (for scraping HebrewBooks.org)
- **Koin** - Dependency injection
- **Coil** - Image loading
- **AndroidX WebView** - PDF viewing (only for PDF display)

## Building

### Prerequisites

- Android Studio (latest version)
- Android SDK 34
- Java 17 or Kotlin 1.9.20+
- Android 7.1+ device or emulator

### Steps

1. Clone this repository
2. Open in Android Studio
3. Sync Gradle
4. Build and run

Or via command line:

```bash
cd android_app
./gradlew assembleDebug
```

### Running

```bash
# Install on connected device
./gradlew installDebug

# Or run directly
./gradlew runDebug
```

## Project Structure

```
android_app/
  app/
    src/main/kotlin/com/hebrewbooks/app/
      models/          # Book, DownloadInfo
      service/         # ApiService, DownloadService
      repository/      # BookRepository
      viewmodel/       # BookViewModel
      di/              # Koin DI
      ui/              # Compose UI
        screens/     # All app screens
        theme/       # Theme & colors
      MainActivity.kt
    src/main/res/        # Resources
  build.gradle
```

## How It Works

- Scrapes HebrewBooks.org for book data
- PDF URLs: `https://hebrewbooks.org/pdf/{ID}.pdf`
- Downloads via Android's DownloadManager
- PDF viewing via WebView (display only)

## Customization

- Colors: Edit `app/src/main/kotlin/com/hebrewbooks/app/ui/theme/Color.kt`
- Typography: Edit `app/src/main/kotlin/com/hebrewbooks/app/ui/theme/Type.kt`
- API URLs: Edit `app/src/main/kotlin/com/hebrewbooks/app/service/ApiService.kt`

## Permissions

- **INTERNET** - For downloading books and data
- **WRITE_EXTERNAL_STORAGE** - For saving PDFs (Android 7-10)
- **READ_EXTERNAL_STORAGE** - For accessing saved PDFs (Android 7-10)
- **MANAGE_EXTERNAL_STORAGE** - For Android 11+ (optional, with legacy flag)

## Limitations

1. **No Official API** - HebrewBooks.org doesn't have a public API, so we scrape the website
2. **PDF Viewer** - Uses WebView for PDF display (but NOT for browsing the website)
3. **Search** - Limited to what's available on the website

## Future Improvements

- Add bookmarks/favorites
- Add reading progress tracking
- Add night mode
- Add text-to-speech
- Add Hebrew keyboard support
- Add offline search
- Add book categories
- Add recent books

## License

This app is for educational purposes and uses content from HebrewBooks.org, which is available for free non-commercial use.

## Credits

- HebrewBooks.org for the amazing collection of Hebrew books
- Android & Jetpack Compose teams for the excellent frameworks
- All open-source libraries used in this project
