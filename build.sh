#!/bin/bash

# HebrewBooks Android App Build Script

echo "=========================================="
echo "HebrewBooks Native Android App Builder"
echo "=========================================="
echo ""

# Check if we're in the right directory
if [ ! -d "android_app" ]; then
    echo "Error: Please run this script from the project root"
    exit 1
fi

cd android_app

echo "Building HebrewBooks Android App..."
echo ""

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Sync Gradle
echo "Syncing Gradle..."
./gradlew --refresh-dependencies

# Build debug APK
echo "Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    
    # Check if device is connected
    if adb devices | grep -q "device"; then
        echo "Device detected. Installing..."
        adb install -r app/build/outputs/apk/debug/app-debug.apk
        if [ $? -eq 0 ]; then
            echo "✅ App installed successfully!"
        else
            echo "⚠️ Installation failed. Try manually:"
            echo "   adb install app/build/outputs/apk/debug/app-debug.apk"
        fi
    else
        echo "No device connected. To install manually:"
        echo "  1. Connect your Android device (API 25+)"
        echo "  2. Enable USB debugging"
        echo "  3. Run: adb install app/build/outputs/apk/debug/app-debug.apk"
    fi
else
    echo ""
    echo "❌ Build failed!"
    echo "Check the error messages above"
    exit 1
fi

echo ""
echo "=========================================="
