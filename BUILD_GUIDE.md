# HebrewBooks Android App - Build Guide

## Quick Start

### Method 1: Using Android Studio (Recommended)

1. **Download Android Studio**
   - Get it from: https://developer.android.com/studio
   - Install it on your computer

2. **Open the Project**
   - Launch Android Studio
   - Click "Open" and select the `hebrewbooks_app/android_app` folder
   - Wait for Gradle to sync (this may take a few minutes)

3. **Build and Run**
   - Connect your Android device (API 25+) via USB
   - Or create an Android emulator (AVD) with API 25+ (Android 7.1)
   - Click the green "Run" button (▶️)
   - Select your device/emulator
   - The app will build and install automatically

### Method 2: Command Line Build

#### Prerequisites

1. **Install Java JDK 17**
   ```bash
   # On Ubuntu/Debian
   sudo apt update
   sudo apt install openjdk-17-jdk
   
   # On macOS (using Homebrew)
   brew install openjdk@17
   
   # On Windows
   Download from: https://adoptium.net/
   ```

2. **Install Android SDK**
   ```bash
   # Install Android SDK Command Line Tools
   # Download from: https://developer.android.com/studio#command-tools
   
   # Or on Ubuntu/Debian
   sudo apt install android-sdk
   ```

3. **Set Environment Variables**
   ```bash
   # Add to your ~/.bashrc or ~/.zshrc
   export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
   export ANDROID_HOME=$HOME/Android/Sdk
   export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
   export PATH=$PATH:$ANDROID_HOME/platform-tools
   export PATH=$PATH:$ANDROID_HOME/emulator
   ```

4. **Install Required SDK Packages**
   ```bash
   sdkmanager "platform-tools"
   sdkmanager "platforms;android-34"
   sdkmanager "platforms;android-25"
   sdkmanager "build-tools;34.0.0"
   sdkmanager "emulator"
   sdkmanager "patcher;v4"
   ```

#### Build the App

```bash
# Navigate to the project
cd hebrewbooks_app/android_app

# Make gradlew executable (if not already)
chmod +x gradlew

# Build debug APK
./gradlew clean assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

#### Install on Device

```bash
# Connect your Android device via USB
# Enable USB debugging on your device

# Install the APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Or run directly (builds and installs)
./gradlew installDebug
```

### Method 3: Using the Build Script

```bash
# Navigate to project root
cd hebrewbooks_app

# Make build script executable
chmod +x build.sh

# Run the build script
./build.sh

# This will:
# 1. Clean previous builds
# 2. Sync Gradle
# 3. Build debug APK
# 4. Install on connected device (if available)
```

## Troubleshooting

### Issue: Gradle Wrapper Missing

**Error**: `Could not find or load main class org.gradle.wrapper.GradleWrapperMain`

**Solution**:
```bash
cd hebrewbooks_app/android_app
./gradlew wrapper --gradle-version 8.3
```

Or manually download:
```bash
mkdir -p gradle/wrapper/dists/gradle-8.3-bin
cd gradle/wrapper/dists/gradle-8.3-bin
wget https://services.gradle.org/distributions/gradle-8.3-bin.zip
unzip gradle-8.3-bin.zip
```

### Issue: Java Not Found

**Error**: `ERROR: JAVA_HOME is not set and no 'java' command could be found`

**Solution**:
```bash
# Install Java 17
echo "export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))" >> ~/.bashrc
source ~/.bashrc
```

### Issue: Android SDK Not Found

**Error**: `Failed to find Platform SDK with path: platforms/android-34`

**Solution**:
```bash
sdkmanager "platforms;android-34"
sdkmanager "platforms;android-25"
```

### Issue: Build Fails with Kotlin Errors

**Error**: `Could not find org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20`

**Solution**:
```bash
# Update Gradle
./gradlew wrapper --gradle-version 8.3

# Or manually check Kotlin version compatibility
# Edit android_app/app/build.gradle and change kotlin version
```

### Issue: Compose Compiler Error

**Error**: `Could not find org.jetbrains.kotlinx:kotlinx-compose-compiler-embeddable:1.5.4`

**Solution**:
```bash
# Update Compose compiler version in android_app/app/build.gradle
# Change: kotlinCompilerExtensionVersion '1.5.4' to a newer version
```

### Issue: DownloadManager Permission

**Error**: App crashes when trying to download

**Solution**:
```bash
# On Android 10+, you need to request storage permissions
# The app already handles this, but you need to grant permissions
# when prompted
```

## Build Variants

### Debug Build
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
```bash
# First, set up signing (optional for testing)
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Bundle (for Google Play)
```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

## Testing

### Run All Tests
```bash
./gradlew test
```

### Run Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

## Clean Build

```bash
# Clean all build files
./gradlew clean

# Clean and rebuild
./gradlew clean assembleDebug
```

## Project Structure Verification

Before building, verify the project structure:

```bash
cd hebrewbooks_app/android_app

# Check all Kotlin files exist
find app/src/main/kotlin -name "*.kt" | sort

# Check all resource files exist
find app/src/main/res -type f | sort

# Check build files
ls -la *.gradle settings.gradle
```

You should see:
- 10+ Kotlin files in `app/src/main/kotlin/com/hebrewbooks/app/`
- 7+ resource files in `app/src/main/res/`
- 3 gradle files in the root

## Expected Output

After a successful build, you'll find:

```
app/build/outputs/apk/debug/
  app-debug.apk          # The debug APK file
  output.json            # Build metadata
```

## First Run

When you first run the app:

1. **Grant Permissions**: The app will request storage permissions for downloading PDFs
2. **Featured Books**: You'll see a grid of featured books from HebrewBooks.org
3. **Search**: Tap the search bar to search for books
4. **Download**: Tap any book, then tap "Download PDF"
5. **Read**: After download, tap "Open PDF" to read

## Performance Tips

1. **Faster Builds**: Use `--parallel` and `--daemon`
   ```bash
   ./gradlew assembleDebug --parallel --daemon
   ```

2. **Offline Builds**: Use `--offline` to avoid downloading dependencies
   ```bash
   ./gradlew assembleDebug --offline
   ```

3. **Incremental Builds**: Gradle automatically uses incremental builds

## Memory Settings

If you get out of memory errors:

```bash
# In gradle.properties, add:
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
```

## Environment Variables

For better performance, set these in your shell:

```bash
export GRADLE_OPTS="-Xmx4g -XX:MaxMetaspaceSize=1g"
export ANDROID_NDK_HOME=$ANDROID_HOME/ndk/<version>
```

## Final Notes

- The first build will take longer as it downloads all dependencies
- Subsequent builds will be much faster
- If you encounter any issues, check the error message and refer to this guide
- The app is designed for Android 7.1+ (API 25+)

## Success! 🎉

Once built, you'll have a fully functional HebrewBooks app that:
- ✅ Is 100% native (no WebView for browsing)
- ✅ Can download PDFs directly
- ✅ Has smooth, easy search
- ✅ Has a clean, modern UI
- ✅ Works offline after downloading
