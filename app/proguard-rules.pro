# Keep Shizuku API surface. Shizuku uses AIDL/reflection and must not be
# stripped or renamed by R8.
-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-keep class dev.rikka.shizuku.** { *; }

# Keep serialized models (kotlinx.serialization uses reflection-free codegen,
# but keep the model classes defensively).
-keepclassmembers class com.modutrap.** {
    *** Companion;
}
-keepclasseswithmembers class com.modutrap.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Compose / AndroidX defaults.
-dontwarn androidx.compose.**
-dontwarn org.jetbrains.compose.**
