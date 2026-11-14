package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.viewmodel.CabinTheme

/**
 * Represents an elevator model with unique physical specs,
 * capacity, speed dynamics, and visual styling.
 */
enum class ElevatorModel(
    val id: String,
    val modelName: String,
    val manufacturer: String,
    val tagline: String,
    val emoji: String,
    // Dynamics & Specs
    val topSpeedKmh: Int,
    val stepDelayMs: Long,
    val maxPassengers: Int,
    val maxPayloadKg: Int,
    val accelerationFeel: String,
    // Visual Styling Tokens
    val primaryAccentColor: Long,
    val secondaryAccentColor: Long,
    val wallColorDark: Long,
    val wallColorLight: Long,
    val ceilingGlowColor: Long,
    val doorColorStart: Long,
    val doorColorEnd: Long,
    val badgeBorderColor: Long,
    val styleCategory: String,
    val cabinTheme: CabinTheme
) {
    SKYLINE_PANORAMA(
        id = "skyline_panorama",
        modelName = "Skyline Panorama-Express",
        manufacturer = "SkyTech Aerolift AG",
        tagline = "Aerodynamische Glaskabine mit Weitblick & sanftem Express-Gleiten",
        emoji = "🏙️",
        topSpeedKmh = 95,
        stepDelayMs = 100L,
        maxPassengers = 24,
        maxPayloadKg = 1800,
        accelerationFeel = "Butterweich mit Magnetschwebe-Dämpfung",
        primaryAccentColor = 0xFF00F0FF,
        secondaryAccentColor = 0xFF38BDF8,
        wallColorDark = 0xFF0F2238,
        wallColorLight = 0xFF1E3A5F,
        ceilingGlowColor = 0xFFE0F2FE,
        doorColorStart = 0xFF1E3A5F,
        doorColorEnd = 0xFF0E7490,
        badgeBorderColor = 0xFF00F0FF,
        styleCategory = "Panorama & Glas",
        cabinTheme = CabinTheme.SPACE_ORBIT
    ),

    HERITAGE_VINTAGE(
        id = "heritage_vintage",
        modelName = "Kaiserhof Heritage 1898",
        manufacturer = "Gebrüder Otis & Sohn, Wien 1898",
        tagline = "Polierte Edelhölzer, Messing-Beschläge & nostalgischer Salon-Komfort",
        emoji = "👑",
        topSpeedKmh = 38,
        stepDelayMs = 240L,
        maxPassengers = 6,
        maxPayloadKg = 500,
        accelerationFeel = "Gemächlich mit klassischem Riemenantrieb",
        primaryAccentColor = 0xFFFFD166,
        secondaryAccentColor = 0xFFDDA15E,
        wallColorDark = 0xFF2A150A,
        wallColorLight = 0xFF4A2810,
        ceilingGlowColor = 0xFFFEF3C7,
        doorColorStart = 0xFF3E1F0D,
        doorColorEnd = 0xFF8C531B,
        badgeBorderColor = 0xFFFFD166,
        styleCategory = "Klassisch & Luxus",
        cabinTheme = CabinTheme.RETRO_CLASSIC
    ),

    TITAN_FREIGHT(
        id = "titan_freight",
        modelName = "Titan Heavy-Duty 5000",
        manufacturer = "Krupp Thyssen Industrietechnik",
        tagline = "Verstärkter Industrie-Schwerlastaufzug mit Riffelblech & Sicherheitsstahldämpfung",
        emoji = "🏗️",
        topSpeedKmh = 55,
        stepDelayMs = 160L,
        maxPassengers = 40,
        maxPayloadKg = 3500,
        accelerationFeel = "Kraftvoll & unerschütterlich",
        primaryAccentColor = 0xFFF59E0B,
        secondaryAccentColor = 0xFFEAB308,
        wallColorDark = 0xFF1E293B,
        wallColorLight = 0xFF334155,
        ceilingGlowColor = 0xFFCBD5E1,
        doorColorStart = 0xFF1F2937,
        doorColorEnd = 0xFF374151,
        badgeBorderColor = 0xFFF59E0B,
        styleCategory = "Industrie & Schwerlast",
        cabinTheme = CabinTheme.GOLDEN_LUXURY
    ),

    CYBER_MAGLEV(
        id = "cyber_maglev",
        modelName = "Cyber-Vortex MagLev 2077",
        manufacturer = "Neo-Shinjuku Quantum Transit",
        tagline = "Supraleitender Magnetschwebe-Aufzug mit HUD-Hologramm & Warp-Tempo",
        emoji = "⚡",
        topSpeedKmh = 160,
        stepDelayMs = 50L,
        maxPassengers = 16,
        maxPayloadKg = 1200,
        accelerationFeel = "Blitzschnell, gravitationskompensiert",
        primaryAccentColor = 0xFFFF007F,
        secondaryAccentColor = 0xFF00F0FF,
        wallColorDark = 0xFF090D16,
        wallColorLight = 0xFF141E33,
        ceilingGlowColor = 0xFFF472B6,
        doorColorStart = 0xFF0F172A,
        doorColorEnd = 0xFF1E1B4B,
        badgeBorderColor = 0xFFFF007F,
        styleCategory = "Sci-Fi & Cyberpunk",
        cabinTheme = CabinTheme.NEON_CYBERPUNK
    );

    companion object {
        val DEFAULT = SKYLINE_PANORAMA

        fun fromId(id: String?): ElevatorModel {
            return entries.firstOrNull { it.id == id } ?: DEFAULT
        }
    }
}
