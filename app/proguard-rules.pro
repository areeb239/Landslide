# ProGuard Rules for BhuRakshak NER Landslide Warning

# Keep Retrofit and Gson Models / DTOs (prevent JSON field name obfuscation)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

-keep class com.ner.landslide.data.remote.api.** { *; }
-keep class com.ner.landslide.domain.model.** { *; }

# Gson
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Coroutines
-dontwarn kotlinx.coroutines.**

# Room Database
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * {
    @androidx.room.Dao *;
}

# Firebase & Play Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
