package com.example.model

/**
 * Available background music tracks for the elevator cabin.
 * Each track has distinct musical characteristics, tempo, and atmosphere.
 */
enum class ElevatorMusicTrack(
    val id: String,
    val title: String,
    val genre: String,
    val emoji: String,
    val bpm: Int,
    val description: String
) {
    BOSSA_BREEZE(
        id = "bossa_breeze",
        title = "Bossa Breeze",
        genre = "Lounge Jazz & Bossa Nova",
        emoji = "🎷",
        bpm = 112,
        description = "Klassischer Fahrstuhl-Jazz mit samtigen E-Piano-Akkorden & Walking Bass"
    ),
    LOFI_SKYMALL(
        id = "lofi_skymall",
        title = "Lofi Skymall",
        genre = "Chillhop & Vinyl Vibes",
        emoji = "☕",
        bpm = 84,
        description = "Entspannte Lo-Fi Beats mit warmen Jazz-Chords und gemütlichem Vinyl-Knistern"
    ),
    VAPOR_HORIZON(
        id = "vapor_horizon",
        title = "Vapor Horizon",
        genre = "80s City Pop / Synthwave",
        emoji = "🌆",
        bpm = 126,
        description = "Funkiger 80er Jahre Glitzer-Synthwave mit treibender Bassline und Neon-Atmosphäre"
    ),
    RETRO_MUZAK(
        id = "retro_muzak",
        title = "Retro Muzak",
        genre = "70s Kaufhaus-Orgel Easy Listening",
        emoji = "🛒",
        bpm = 120,
        description = "Nostalgische Kaufhaus-Kaufmusik mit fröhlichen Orgel-Stabs und heiterer Melodie"
    ),
    ZEN_CLOUD(
        id = "zen_cloud",
        title = "Zen Cloudscape",
        genre = "Atmospheric Spa & Ambient",
        emoji = "☁️",
        bpm = 68,
        description = "Schwebende, verträumte Klangwolken und sanfte Glockenspiele für maximale Ruhe"
    );

    fun next(): ElevatorMusicTrack {
        val values = entries
        val nextIndex = (ordinal + 1) % values.size
        return values[nextIndex]
    }

    fun previous(): ElevatorMusicTrack {
        val values = entries
        val prevIndex = if (ordinal - 1 < 0) values.size - 1 else ordinal - 1
        return values[prevIndex]
    }
}
