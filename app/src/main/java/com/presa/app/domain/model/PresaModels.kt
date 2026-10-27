package com.presa.app.domain.model

enum class AppMode {
    VISUALIZER,
    SLIDES,
}

enum class SlidesSubMode {
    POWERPOINT,
    PDF,
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class AppLanguage(val tag: String) {
    FRENCH("fr"),
    ENGLISH("en");

    companion object {
        fun fromTag(tag: String?): AppLanguage =
            entries.firstOrNull { it.tag == tag } ?: FRENCH
    }
}

enum class RepeatSpeed(
    val initialDelayMs: Long,
    val intervalMs: Long,
) {
    SLOW(500L, 190L),
    NORMAL(400L, 120L),
    FAST(300L, 75L),
}
