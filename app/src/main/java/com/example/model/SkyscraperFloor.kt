package com.example.model

/**
 * Category classification for mall floors in the skyscraper.
 */
enum class FloorCategory(
    val title: String,
    val emoji: String,
    val colorHex: Long
) {
    FOOD_DINING("Gastronomie & Snacks", "🍔", 0xFFFF6584),
    TOYS_LEISURE("Spielzeug & Spaß", "🧸", 0xFFFF9F1C),
    GAMING_TECH("Gaming & VR", "🎮", 0xFF7209B7),
    ANIMALS_SAFARI("Dinos & Tiere", "🦖", 0xFF2EC4B6),
    SPORTS_STREET("Action & Skate", "🛹", 0xFFE63946),
    CYBER_ROBOTICS("Roboter & Cyber-Labor", "🤖", 0xFF00B4D8),
    AMUSEMENT_CINEMA("Freizeitpark & 4D-Kino", "🎢", 0xFFFF007F),
    SKY_COSMOS("Weltraum & Sky-Deck", "🌌", 0xFF3A0CA3)
}

/**
 * Visitor density and crowd activity level for a floor.
 */
enum class FloorCrowdLevel(
    val label: String,
    val emoji: String
) {
    QUIET("Ruhig & entspannt", "🟢"),
    MODERATE("Angenehm besucht", "🟡"),
    LIVELY("Sehr belebt", "🟠"),
    PACKED("Großer Andrang / Party", "🔥")
}

/**
 * Quick jump sector grouping the 120 floors into navigable sections.
 */
data class FloorSector(
    val id: String,
    val range: IntRange,
    val title: String,
    val emoji: String,
    val subtitle: String
)

/**
 * Rich, structured representation of an individual floor in the 100+ floor skyscraper mall.
 */
data class SkyscraperFloor(
    val floorNumber: Int,
    val name: String,
    val tagline: String,
    val zone: MallZone,
    val category: FloorCategory,
    val altitudeMeters: Int,
    val isLandmark: Boolean,
    val landmarkTitle: String? = null,
    val emoji: String,
    val shopCount: Int,
    val highlights: List<String>,
    val crowdLevel: FloorCrowdLevel,
    val hasCoinFountain: Boolean,
    val primaryColor: Long,
    val secondaryColor: Long
) {
    /**
     * Estimates elevator travel time in seconds between this floor and a departure floor.
     */
    fun estimateTravelSeconds(fromFloor: Int, speedMultiplier: Float = 1.0f): Int {
        val distance = kotlin.math.abs(floorNumber - fromFloor)
        if (distance == 0) return 0
        val baseSecPerFloor = 0.12f / speedMultiplier
        val totalSec = (distance * baseSecPerFloor + 0.8f).toInt()
        return totalSec.coerceAtLeast(1)
    }

    /**
     * Altitude difference in meters relative to another floor.
     */
    fun altitudeDeltaMeters(fromFloorNumber: Int): Int {
        return (floorNumber - fromFloorNumber) * 4
    }
}

/**
 * Central registry and catalog provider for the 100+ skyscraper floors.
 */
object SkyscraperFloorRegistry {
    const val TOTAL_FLOORS = 120
    const val BASE_ALTITUDE_METERS = 12
    const val METERS_PER_FLOOR = 4

    val SECTORS = listOf(
        FloorSector("sec_1", 1..20, "1–20", "🍭", "Lobby & Food Court"),
        FloorSector("sec_2", 21..40, "21–40", "🧸", "Spielzeug-Königreich"),
        FloorSector("sec_3", 41..60, "41–60", "🎮", "Arcade & Dinos"),
        FloorSector("sec_4", 61..80, "61–80", "🛹", "Skater & Cyber-Labor"),
        FloorSector("sec_5", 81..100, "81–100", "🎢", "Freizeitpark & Jubiläum"),
        FloorSector("sec_6", 101..120, "101–120", "🚀", "Sky-Deck & Weltraum")
    )

    private val allFloorsList: List<SkyscraperFloor> by lazy {
        (1..TOTAL_FLOORS).map { floorNum ->
            buildFloor(floorNum)
        }
    }

    fun getAllFloors(): List<SkyscraperFloor> = allFloorsList

    fun getFloor(floorNumber: Int): SkyscraperFloor {
        val clamped = floorNumber.coerceIn(1, TOTAL_FLOORS)
        return allFloorsList[clamped - 1]
    }

    fun getLandmarkFloors(): List<SkyscraperFloor> {
        return allFloorsList.filter { it.isLandmark }
    }

    fun getFountainFloors(): List<SkyscraperFloor> {
        return allFloorsList.filter { it.hasCoinFountain }
    }

    fun getSectorForFloor(floorNumber: Int): FloorSector {
        return SECTORS.firstOrNull { floorNumber in it.range } ?: SECTORS.first()
    }

    fun filterFloors(
        query: String = "",
        sector: FloorSector? = null,
        category: FloorCategory? = null,
        landmarksOnly: Boolean = false,
        fountainsOnly: Boolean = false,
        visitedOnly: Boolean = false,
        visitedFloors: Set<Int> = emptySet()
    ): List<SkyscraperFloor> {
        val cleanQuery = query.trim().lowercase()

        return allFloorsList.filter { floor ->
            // Sector match
            if (sector != null && floor.floorNumber !in sector.range) {
                return@filter false
            }

            // Category match
            if (category != null && floor.category != category) {
                return@filter false
            }

            // Landmark filter
            if (landmarksOnly && !floor.isLandmark) {
                return@filter false
            }

            // Fountain filter
            if (fountainsOnly && !floor.hasCoinFountain) {
                return@filter false
            }

            // Visited filter
            if (visitedOnly && floor.floorNumber !in visitedFloors) {
                return@filter false
            }

            // Text search match
            if (cleanQuery.isNotEmpty()) {
                val matchesNumber = floor.floorNumber.toString() == cleanQuery ||
                    floor.floorNumber.toString().startsWith(cleanQuery)
                val matchesName = floor.name.lowercase().contains(cleanQuery)
                val matchesTagline = floor.tagline.lowercase().contains(cleanQuery)
                val matchesZone = floor.zone.title.lowercase().contains(cleanQuery)
                val matchesHighlights = floor.highlights.any { it.lowercase().contains(cleanQuery) }
                val matchesLandmark = floor.landmarkTitle?.lowercase()?.contains(cleanQuery) == true

                if (!matchesNumber && !matchesName && !matchesTagline && !matchesZone && !matchesHighlights && !matchesLandmark) {
                    return@filter false
                }
            }

            true
        }
    }

    private fun buildFloor(floor: Int): SkyscraperFloor {
        val zone = MallCatalog.getZoneForFloor(floor)
        val altitude = BASE_ALTITUDE_METERS + (floor - 1) * METERS_PER_FLOOR
        val hasFountain = (floor % 10 == 0)

        // Landmarks definition
        val isLandmark: Boolean
        val landmarkTitle: String?
        val specialName: String
        val tagline: String
        val emoji: String
        val category: FloorCategory
        val highlights: List<String>
        val crowd: FloorCrowdLevel

        when (floor) {
            1 -> {
                isLandmark = true
                landmarkTitle = "★ Grand Mall Lobby"
                specialName = "Grand Mall Lobby & Candy Paradise"
                tagline = "Haupteingang des Wolkenkratzers mit Mega-Bonbonglas und Info-Desk"
                emoji = "🍬"
                category = FloorCategory.FOOD_DINING
                highlights = listOf("Empfangshalle", "Bonbon-Paradies", "Touristen-Info")
                crowd = FloorCrowdLevel.PACKED
            }
            10 -> {
                isLandmark = true
                landmarkTitle = "★ Riesen-Schoko-Brunnen"
                specialName = "Schokoladen-Brunnen Piazza"
                tagline = "Fließende Alpenmilch-Schokolade mit Erdbeer-Spießen und Waffel-Station"
                emoji = "🍫"
                category = FloorCategory.FOOD_DINING
                highlights = listOf("Schoko-Wasserfall", "Gratis-Münzen", "Waffelbäckerei")
                crowd = FloorCrowdLevel.PACKED
            }
            25 -> {
                isLandmark = true
                landmarkTitle = "★ Baustein Giganten-Welt"
                specialName = "Baustein Giganten-Palast"
                tagline = "Überlebensgroße Lego-Statuen, Bautische und interaktive Modelle"
                emoji = "🧱"
                category = FloorCategory.TOYS_LEISURE
                highlights = listOf("Lego-Drache", "Freies Bauen", "Rennbahn")
                crowd = FloorCrowdLevel.LIVELY
            }
            42 -> {
                isLandmark = true
                landmarkTitle = "★ Retro Arcade Arena"
                specialName = "Geheimlevel – Pixel Arcade"
                tagline = "Kultige Münzautomaten, Flipperkugeln, Street Fighter & Neon-Chiptunes"
                emoji = "👾"
                category = FloorCategory.GAMING_TECH
                highlights = listOf("Pac-Man Classic", "VR-Kapseln", "Highscore-Wand")
                crowd = FloorCrowdLevel.PACKED
            }
            50 -> {
                isLandmark = true
                landmarkTitle = "★ T-Rex Gehege"
                specialName = "Urzeit T-Rex Gehege & Fossilien-Dom"
                tagline = "Animatronischer Tyrannosaurus Rex in Lebensgröße mit Brüll-Show"
                emoji = "🦖"
                category = FloorCategory.ANIMALS_SAFARI
                highlights = listOf("T-Rex Show", "Fossilien-Grabung", "Kuschel-Dinos")
                crowd = FloorCrowdLevel.LIVELY
            }
            69 -> {
                isLandmark = true
                landmarkTitle = "★ Neon Skater Halfpipe"
                specialName = "Neon Skater-Bowl & Streetwear"
                tagline = "Indoor-Skatepark mit leuchtenden LED-Rampen und Sneaker-Customizing"
                emoji = "🛹"
                category = FloorCategory.SPORTS_STREET
                highlights = listOf("Halfpipe", "Sneaker-Wall", "DJ-Booth")
                crowd = FloorCrowdLevel.LIVELY
            }
            77 -> {
                isLandmark = true
                landmarkTitle = "★ Cyber-Bot Werkstatt"
                specialName = "Cyber-Bot Zukunftslabor 2099"
                tagline = "Sprechende Androiden, Hologramm-Haustiere und DIY-Roboterbausätze"
                emoji = "🤖"
                category = FloorCategory.CYBER_ROBOTICS
                highlights = listOf("Robo-Haustiere", "Laser-Schneiden", "AI-Demo")
                crowd = FloorCrowdLevel.MODERATE
            }
            99 -> {
                isLandmark = true
                landmarkTitle = "★ Sky Coaster Bahnhof"
                specialName = "Achterbahn-Station im Himmel"
                tagline = "Startpunkt der spektakulären Wolkenkratzer-Außenachterbahn"
                emoji = "🎢"
                category = FloorCategory.AMUSEMENT_CINEMA
                highlights = listOf("Looping Coaster", "Zuckerwatte", "Foto-Station")
                crowd = FloorCrowdLevel.PACKED
            }
            100 -> {
                isLandmark = true
                landmarkTitle = "★ Hundert-Etagen Jubiläum"
                specialName = "Etage 100: Jubiläums-Feuerwerk 🎆"
                tagline = "Großes Festdeck mit Konfetti, Feuerwerks-Show und Jubiläums-Belohnung"
                emoji = "🎆"
                category = FloorCategory.AMUSEMENT_CINEMA
                highlights = listOf("Feuerwerk", "Jubiläumsbrunnen", "Festsaal")
                crowd = FloorCrowdLevel.PACKED
            }
            111 -> {
                isLandmark = true
                landmarkTitle = "★ Galaktische Sternenwarte"
                specialName = "Cosmo-Sternwarte & Planetarium"
                tagline = "Riesiges Spiegelteleskop mit Live-Blick auf Saturnringe und Mondkrater"
                emoji = "🔭"
                category = FloorCategory.SKY_COSMOS
                highlights = listOf("Weltraum-Teleskop", "Meteoriten-Stücke", "Alien-Café")
                crowd = FloorCrowdLevel.QUIET
            }
            120 -> {
                isLandmark = true
                landmarkTitle = "★ Höchstes Sky-Deck"
                specialName = "Sky-Deck & VIP Cosmic Penthouse"
                tagline = "Die absolute Spitze auf 488 Metern Höhe mit Glasboden über den Wolken"
                emoji = "🚀"
                category = FloorCategory.SKY_COSMOS
                highlights = listOf("Glasboden-Skywalk", "360° Panorama", "VIP Lounge")
                crowd = FloorCrowdLevel.PACKED
            }
            else -> {
                isLandmark = false
                landmarkTitle = null
                when (zone.id) {
                    "sweets" -> {
                        specialName = "Etage $floor: ${getSweetsTitle(floor)}"
                        tagline = "Süße Köstlichkeiten, Erfrischungen und Schlemmer-Stationen"
                        emoji = when (floor % 4) {
                            0 -> "🍦"
                            1 -> "🍭"
                            2 -> "🧁"
                            else -> "🥞"
                        }
                        category = FloorCategory.FOOD_DINING
                        highlights = listOf("Nasch-Bar", "Softdrinks", "Café-Sitzplätze")
                        crowd = if (floor % 3 == 0) FloorCrowdLevel.LIVELY else FloorCrowdLevel.MODERATE
                    }
                    "toys" -> {
                        specialName = "Etage $floor: ${getToysTitle(floor)}"
                        tagline = "Spielewelten, Bastelspass und Abenteuer für Jung und Alt"
                        emoji = when (floor % 4) {
                            0 -> "🧸"
                            1 -> "🚗"
                            2 -> "🪁"
                            else -> "🎲"
                        }
                        category = FloorCategory.TOYS_LEISURE
                        highlights = listOf("Spielzone", "Test-Station", "Geschenkverpackung")
                        crowd = if (floor % 2 == 0) FloorCrowdLevel.LIVELY else FloorCrowdLevel.MODERATE
                    }
                    "gaming" -> {
                        specialName = "Etage $floor: ${getGamingTitle(floor)}"
                        tagline = "Konsolen, Virtual Reality, E-Sports und Neon-Gaming"
                        emoji = when (floor % 4) {
                            0 -> "🎮"
                            1 -> "🕹️"
                            2 -> "🎧"
                            else -> "💻"
                        }
                        category = FloorCategory.GAMING_TECH
                        highlights = listOf("Gaming-Stationen", "VR-Headsets", "Snack-Corner")
                        crowd = if (floor % 5 == 0) FloorCrowdLevel.PACKED else FloorCrowdLevel.LIVELY
                    }
                    "dino_pets" -> {
                        specialName = "Etage $floor: ${getDinoTitle(floor)}"
                        tagline = "Faszinierende Urzeit-Geschöpfe und niedliche Tierbegleiter"
                        emoji = when (floor % 4) {
                            0 -> "🦕"
                            1 -> "🐶"
                            2 -> "🐱"
                            else -> "🦜"
                        }
                        category = FloorCategory.ANIMALS_SAFARI
                        highlights = listOf("Streichelgehege", "Tierfutter-Station", "Souvenirs")
                        crowd = FloorCrowdLevel.MODERATE
                    }
                    "action_sport" -> {
                        specialName = "Etage $floor: ${getSportTitle(floor)}"
                        tagline = "Sportgeräte, Streetwear, Kletterwände und Trend-Ausrüstung"
                        emoji = when (floor % 4) {
                            0 -> "🏀"
                            1 -> "🛹"
                            2 -> "👟"
                            else -> "🧗"
                        }
                        category = FloorCategory.SPORTS_STREET
                        highlights = listOf("Teststrecke", "Fit-Bar", "Umkleiden")
                        crowd = if (floor % 3 == 0) FloorCrowdLevel.LIVELY else FloorCrowdLevel.MODERATE
                    }
                    "robot_tech" -> {
                        specialName = "Etage $floor: ${getRobotTitle(floor)}"
                        tagline = "Neueste Elektronik, Drohnen, Gadgets und Smart Home"
                        emoji = when (floor % 4) {
                            0 -> "🤖"
                            1 -> "🛰️"
                            2 -> "🔋"
                            else -> "⚡"
                        }
                        category = FloorCategory.CYBER_ROBOTICS
                        highlights = listOf("Drohnen-Käfig", "3D-Drucker", "Tech-Support")
                        crowd = FloorCrowdLevel.MODERATE
                    }
                    "funfair" -> {
                        specialName = "Etage $floor: ${getFunfairTitle(floor)}"
                        tagline = "Fahrgeschäfte, Popcorn-Düfte, Zauberei und Kirmesspaß"
                        emoji = when (floor % 4) {
                            0 -> "🍿"
                            1 -> "🎪"
                            2 -> "🎯"
                            else -> "🎡"
                        }
                        category = FloorCategory.AMUSEMENT_CINEMA
                        highlights = listOf("Fotobox", "Popcorn-Stand", "Losbude")
                        crowd = FloorCrowdLevel.LIVELY
                    }
                    else -> {
                        specialName = "Etage $floor: ${getCosmoTitle(floor)}"
                        tagline = "Schwindelerregende Aussichten und astronomische Wunder"
                        emoji = when (floor % 4) {
                            0 -> "🌌"
                            1 -> "⭐"
                            2 -> "🪐"
                            else -> "🛸"
                        }
                        category = FloorCategory.SKY_COSMOS
                        highlights = listOf("Fernrohre", "Sternenkarte", "Ruhezone")
                        crowd = if (floor > 115) FloorCrowdLevel.LIVELY else FloorCrowdLevel.QUIET
                    }
                }
            }
        }

        return SkyscraperFloor(
            floorNumber = floor,
            name = specialName,
            tagline = tagline,
            zone = zone,
            category = category,
            altitudeMeters = altitude,
            isLandmark = isLandmark,
            landmarkTitle = landmarkTitle,
            emoji = emoji,
            shopCount = if (floor in listOf(1, 10, 25, 42, 50, 69, 77, 99, 100, 111, 120)) 3 else 2,
            highlights = highlights,
            crowdLevel = crowd,
            hasCoinFountain = hasFountain,
            primaryColor = zone.primaryColor,
            secondaryColor = zone.secondaryColor
        )
    }

    private fun getSweetsTitle(floor: Int): String = when (floor % 5) {
        0 -> "Marshmallow & Waffel-Oase"
        1 -> "Donut-Manufaktur & Bubble Tea"
        2 -> "Schoko-Präsentkorb & Macarons"
        3 -> "Gummibärchen-Königreich"
        else -> "Fruchtsaft & Eiscreme-Lounge"
    }

    private fun getToysTitle(floor: Int): String = when (floor % 5) {
        0 -> "Modellbau & Rennwagen-Bahn"
        1 -> "Kuscheltier-Zoo & Puppenhaus"
        2 -> "Puzzle- & Brettspiel-Arkade"
        3 -> "Superhelden Actionfiguren"
        else -> "Zauberkästen & Zauberwerkstatt"
    }

    private fun getGamingTitle(floor: Int): String = when (floor % 5) {
        0 -> "VR-Erlebniswelt Matrix"
        1 -> "Retro-Flipper & Arcade-Cabinets"
        2 -> "E-Sports Gaming Lounge"
        3 -> "Handheld- & Indie-Games"
        else -> "Simulatoren: Formel 1 & Flugzeuge"
    }

    private fun getDinoTitle(floor: Int): String = when (floor % 5) {
        0 -> "Triceratops Wiesen"
        1 -> "Fossilien & Geologie-Labor"
        2 -> "Welpen- & Kätzchen-Café"
        3 -> "Tropische Vögel & Reptilien"
        else -> "Urzeit-Pflanzen & Paläo-Shop"
    }

    private fun getSportTitle(floor: Int): String = when (floor % 5) {
        0 -> "Kletterwand & Boulder-Halle"
        1 -> "Sneaker-Boutique & Street-Style"
        2 -> "Basketball & Streetball Court"
        3 -> "Trampolin-Parcours JumpWorld"
        else -> "BMX- & Skateboard-Werkstatt"
    }

    private fun getRobotTitle(floor: Int): String = when (floor % 5) {
        0 -> "Drohnen-Flugschule & Racing"
        1 -> "Hologramm-Technik & Smart Home"
        2 -> "Kompakt-Androiden Ausstellung"
        3 -> "Elektronik-Bastelecke & DIY"
        else -> "Cyborg-Gadgets & Wearables"
    }

    private fun getFunfairTitle(floor: Int): String = when (floor % 5) {
        0 -> "Geisterbahn Mystery Mansion"
        1 -> "Riesenrad & Kettenkarussell"
        2 -> "4D-Kino mit Wind & Wasser"
        3 -> "Spiegelkabinett & Illusions-Show"
        else -> "Zuckerwatte-Palast & Churros"
    }

    private fun getCosmoTitle(floor: Int): String = when (floor % 5) {
        0 -> "Meteoriten-Ausstellung"
        1 -> "Apollo-Mondlande-Simulator"
        2 -> "Galaxien-Projektion 360°"
        3 -> "Sternen-Café mit Kometen-Drinks"
        else -> "Space-Shuttle Cockpit Tour"
    }
}
