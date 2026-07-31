# Warden ProGuard Rules

# Keep Room entities
-keep class com.warden.rnsl.data.model.** { *; }

# Keep Shizuku classes
-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }

# Keep Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# Keep Kotlin coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep ADB helper
-keep class com.warden.rnsl.adb.** { *; }
