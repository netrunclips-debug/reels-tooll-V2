# ProGuard rules for GestureTikTok

# Keep MediaPipe classes
-keep class com.google.mediapipe.** { *; }
-keepclasseswithmembernames class com.google.mediapipe.** { *; }

# Keep Kotlin specific rules
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }

# Keep Android specific classes
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Keep accessibility service
-keep class com.example.gesturetiktok.accessibility.** { *; }

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
