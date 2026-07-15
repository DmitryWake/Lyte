# ── kotlinx.serialization ────────────────────────────────────────────────────
# Типобезопасные @Serializable route-классы навигации сериализуются в рантайме.
# Без этих правил R8 выкидывает сгенерированные сериализаторы и навигация падает.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Keep `Companion` object fields of serializable classes.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
# Keep `serializer()` on companion objects of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$Companion Companion;
}
-keepclassmembers class <2>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Room ─────────────────────────────────────────────────────────────────────
# @ConstructedBy/RoomDatabaseConstructor + сгенерированные *_Impl должны пережить R8.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class * implements androidx.room.RoomDatabaseConstructor { *; }
-dontwarn androidx.room.paging.**

# ── Koin ─────────────────────────────────────────────────────────────────────
# Koin 4 (constructor DSL) рефлексию не использует — специальных keep-правил не требует.
