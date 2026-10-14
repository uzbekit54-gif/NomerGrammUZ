# ==============================================================================
# NomerGramm UZ - ProGuard / R8 Rules for Release Build
# ==============================================================================

# Gson Serialization and Domain Models
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

-keep class com.example.nomergrammuz.domain.model.** { *; }
-keepclassmembers class com.example.nomergrammuz.domain.model.** { <fields>; }

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# AndroidX Lifecycle & Jetpack Compose
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# ML Kit Common & Text Recognition
-keep class com.google.mlkit.** { *; }
-keep interface com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# CameraX
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }
-keep class androidx.camera.lifecycle.** { *; }
-keep class androidx.camera.view.** { *; }
