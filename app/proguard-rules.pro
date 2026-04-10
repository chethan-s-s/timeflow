# ──────────────────────────────────────────────
# General Android / Kotlin
# ──────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve Kotlin metadata so reflection works correctly
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ──────────────────────────────────────────────
# AndroidX / Jetpack
# ──────────────────────────────────────────────
-keep class androidx.lifecycle.** { *; }

# ──────────────────────────────────────────────
# Room Database
# ──────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers @androidx.room.Dao class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }

# Room KSP-generated implementation classes
-keep class **_Impl { *; }
-keep class **_Impl$* { *; }

# ──────────────────────────────────────────────
# Coil (Image Loading)
# ──────────────────────────────────────────────
-keep class coil.** { *; }
-dontwarn coil.**

# ──────────────────────────────────────────────
# Jetpack Compose
# ──────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ──────────────────────────────────────────────
# App-specific: keep data/widget/receiver classes
# ──────────────────────────────────────────────
-keep class com.example.timeflow.data.** { *; }
-keep class com.example.timeflow.widget.** { *; }
-keep class com.example.timeflow.receiver.** { *; }

# ──────────────────────────────────────────────
# Java Serialization (backup/restore)
# ──────────────────────────────────────────────
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ──────────────────────────────────────────────
# Suppress harmless warnings
# ──────────────────────────────────────────────
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
