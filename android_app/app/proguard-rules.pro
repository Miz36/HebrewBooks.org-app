# Android application proguard rules

# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# android.enableProguardInPath and android.proguard.scopes flags in
# project.properties.

# For native methods, see http://proguard.sourceforge.net/faq.html#native

# With R8 full mode, it is not necessary to keep annotations.
# If you want to keep annotations, add this:
# -keepattributes *Annotation*

# Automatic and manual parsing of XML and JSON files
# Go to https://www.guardsquare.com/proguard/troubleshooting#xml for more details

# AndroidX library
-dontwarn androidx.**
-keep class androidx.** { *; }

# Kotlin
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keep class kotlin.* { *; }

# Coroutines
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepclassmembers class retrofit2.* {
    public private *;
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Coil
-dontwarn coil.**
-keep class coil.** { *; }

# WebView
-dontwarn androidx.webkit.**
-keep class androidx.webkit.** { *; }

# Jsoup
-dontwarn org.jsoup.**
-keep class org.jsoup.** { *; }

# Koin
-dontwarn org.koin.**
-keep class org.koin.** { *; }

# WorkManager
-dontwarn androidx.work.**
-keep class androidx.work.** { *; }

# PermissionX
-dontwarn com.guolindev.permissionx.**
-keep class com.guolindev.permissionx.** { *; }
