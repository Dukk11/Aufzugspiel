package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.R

/**
 * Represents a shopping item that can be bought and interacted with.
 */
data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val emoji: String,
    val category: String, // "food", "toy", "gadget", "wearable", "pet", "magic"
    val actionText: String, // "Essen", "Starten", "Ausprobieren", "Streicheln", "Anziehen"
    val actionFeedback: String // fun message when used
)

/**
 * Represents a store inside the shopping center on a floor.
 */
data class Shop(
    val id: String,
    val name: String,
    val subtitle: String,
    val emoji: String,
    val shopkeeperName: String,
    val shopkeeperAvatar: String,
    val welcomeMessage: String,
    val bannerColor: Long,
    val items: List<ShopItem>
)

/**
 * Represents a Mall Zone grouping floors.
 */
data class MallZone(
    val id: String,
    val title: String,
    val emoji: String,
    val floorRange: IntRange,
    val primaryColor: Long,
    val secondaryColor: Long,
    val description: String
)

/**
 * Represents a specific Floor in the skyscraper mall.
 */
data class MallFloor(
    val floorNumber: Int,
    val name: String,
    val zone: MallZone,
    val description: String,
    val shops: List<Shop>
)

/**
 * Catalog manager generating data for over 100 floors (1 to 120).
 */
object MallCatalog {
    val TOTAL_FLOORS = 120

    val ZONES = listOf(
        MallZone(
            id = "sweets",
            title = "Süßigkeiten & Food Court",
            emoji = "🍭",
            floorRange = 1..15,
            primaryColor = 0xFFFF6584,
            secondaryColor = 0xFFFFD166,
            description = "Der süßeste Ort im Wolkenkratzer mit Eiscreme, Bonbons & Riesen-Burgern!"
        ),
        MallZone(
            id = "toys",
            title = "Spielzeug-Königreich & Fun",
            emoji = "🧸",
            floorRange = 16..30,
            primaryColor = 0xFFFF9F1C,
            secondaryColor = 0xFFFFE494,
            description = "Baustein-Türme, ferngesteuerte Rennwagen, Kuscheltiere und Bausteine!"
        ),
        MallZone(
            id = "gaming",
            title = "Pixel Arcade & VR-World",
            emoji = "🎮",
            floorRange = 31..45,
            primaryColor = 0xFF7209B7,
            secondaryColor = 0xFF4CC9F0,
            description = "Retro-Spieleautomaten, VR-Brillen, Neon-Konsolen und E-Sports!"
        ),
        MallZone(
            id = "dino_pets",
            title = "Dino-Safari & Kuscheltiere",
            emoji = "🦖",
            floorRange = 46..60,
            primaryColor = 0xFF2EC4B6,
            secondaryColor = 0xFF80ED99,
            description = "Lebensechte Baby-Dinos, süße Welpen, Katzen und Urzeit-Fossilien!"
        ),
        MallZone(
            id = "action_sport",
            title = "Skater-Park & Hype Street",
            emoji = "🛹",
            floorRange = 61..75,
            primaryColor = 0xFFE63946,
            secondaryColor = 0xFFF1FAEE,
            description = "Leuchtende Hype-Sneaker, Skateboards, Trampoline und Stunt-Bikes!"
        ),
        MallZone(
            id = "robot_tech",
            title = "Roboter-Labor & Cyber-Tech",
            emoji = "🤖",
            floorRange = 76..90,
            primaryColor = 0xFF00B4D8,
            secondaryColor = 0xFF90E0EF,
            description = "Sprechende Mini-Roboter, Laser-Gadgets und Hologramm-Maschinen!"
        ),
        MallZone(
            id = "funfair",
            title = "Freizeitpark & 4D-Kino",
            emoji = "🎢",
            floorRange = 91..105,
            primaryColor = 0xFFFF007F,
            secondaryColor = 0xFFFFD700,
            description = "Indoor-Achterbahn, Popcorn-Paläste, Zauberei und Gruselbahn!"
        ),
        MallZone(
            id = "sky_cosmos",
            title = "Weltraum-Deck & VIP Sky-Lounge",
            emoji = "🌌",
            floorRange = 106..120,
            primaryColor = 0xFF3A0CA3,
            secondaryColor = 0xFF00F0FF,
            description = "Aussicht über die ganze Welt, Alien-Souvenirs und Sternen-Teleskope!"
        )
    )

    fun getZoneForFloor(floor: Int): MallZone {
        return ZONES.firstOrNull { floor in it.floorRange } ?: ZONES.first()
    }

    fun getZoneDrawableRes(floor: Int): Int {
        return when (getZoneForFloor(floor).id) {
            "sweets" -> R.drawable.img_mall_sweets_1794056123513
            "toys", "gaming", "funfair" -> R.drawable.img_mall_toys_arcade_1794056123393
            "dino_pets" -> R.drawable.img_mall_dino_safari_1794056124750
            "action_sport", "robot_tech" -> R.drawable.img_mall_robot_skate_1794056125177
            else -> R.drawable.img_mall_sky_cosmos_1794056126682
        }
    }

    /**
     * Dynamically builds or retrieves a rich MallFloor with 2 to 3 themed shops.
     */
    fun getFloor(floorNumber: Int): MallFloor {
        val clampedFloor = floorNumber.coerceIn(1, TOTAL_FLOORS)
        val zone = getZoneForFloor(clampedFloor)

        val shops = when (zone.id) {
            "sweets" -> createSweetShops(clampedFloor)
            "toys" -> createToyShops(clampedFloor)
            "gaming" -> createGamingShops(clampedFloor)
            "dino_pets" -> createDinoPetShops(clampedFloor)
            "action_sport" -> createSportShops(clampedFloor)
            "robot_tech" -> createRobotShops(clampedFloor)
            "funfair" -> createFunfairShops(clampedFloor)
            else -> createCosmicShops(clampedFloor)
        }

        val floorName = when (clampedFloor) {
            1 -> "Erdgeschoss: Große Mall-Lobby & Bonbon-Paradies"
            10 -> "Etage 10: Riesen-Schokoladen-Brunnen"
            25 -> "Etage 25: Baustein Giganten-Welt"
            42 -> "Etage 42: Geheimlevel – Retro Arcade Arena"
            50 -> "Etage 50: T-Rex Gehege & Fossilien-Halle"
            69 -> "Etage 69: Neon Skater Halfpipe"
            77 -> "Etage 77: Cyber-Bot Werkstatt der Zukunft"
            99 -> "Etage 99: Achterbahn-Bahnhof im Himmel"
            100 -> "Etage 100: Jubiläums-Etage mit Feuerwerk! 🎆"
            111 -> "Etage 111: Galaktische Sternenwarte"
            120 -> "Etage 120: Das höchste Penthouse-Sky-Deck! 🚀"
            else -> "Etage $clampedFloor: ${zone.title}"
        }

        return MallFloor(
            floorNumber = clampedFloor,
            name = floorName,
            zone = zone,
            description = "Willkommen auf Etage $clampedFloor im Einkaufszentrum! Besuche die Geschäfte und schnapp dir coole Sachen.",
            shops = shops
        )
    }

    private fun createSweetShops(floor: Int): List<Shop> {
        return listOf(
            Shop(
                id = "shop_icecream_$floor",
                name = "Frosty Magic Eis-Palast",
                subtitle = "Eis in 100 verrückten Sorten",
                emoji = "🍦",
                shopkeeperName = "Gusti Gelato",
                shopkeeperAvatar = "🍨",
                welcomeMessage = "Ciao! Willkommen beim buntesten Eisbecher des Wolkenkratzers! Alles handgemacht!",
                bannerColor = 0xFFFF758F,
                items = listOf(
                    ShopItem("ice_1", "Regenbogen-Softeis", "Leuchtet im Dunkeln und schmeckt nach Erdbeere", 15, "🍦", "food", "Auflecken!", "Mmmh, eiskalt und super fruchtig!"),
                    ShopItem("ice_2", "Mega-Vulkan-Eisbecher", "Mit Sternen-Streuseln und Schoko-Lava", 35, "🍨", "food", "Verputzen!", "Wow, ein Gehirnfrost voller Geschmack!"),
                    ShopItem("ice_3", "Goldenes Luxus-Eis", "Mit essbarem Glitzer-Goldstaub verziert", 60, "🍧", "food", "Genießen!", "Du fühlst dich wie ein Milliardär!")
                )
            ),
            Shop(
                id = "shop_candy_$floor",
                name = "Sugar Rush Kaugummi-Oase",
                subtitle = "Gummibärchen in Riesenformat",
                emoji = "🍬",
                shopkeeperName = "Frau Zuckerfee",
                shopkeeperAvatar = "🧚‍♀️",
                welcomeMessage = "Komm rein! Hier klebt der Boden vor lauter zuckriger Fröhlichkeit!",
                bannerColor = 0xFFFFB703,
                items = listOf(
                    ShopItem("candy_1", "Riesen-Gummibär", "So groß wie dein Fuß! Erdbeer-Geschmack", 20, "🧸", "food", "Abbeißen!", "Köstlich saftig und zäh!"),
                    ShopItem("candy_2", "Brause-Knallbonbon", "Explodiert harmlos im Mund mit buntem Prickeln", 12, "💥", "food", "Knallen lassen!", "Zzzzt! Deine Zunge kribbelt wie verrückt!"),
                    ShopItem("candy_3", "Regenbogen-Zuckerwatte", "Fluffig wie eine Wolke im Himmel", 25, "🍭", "food", "Naschen!", "Sie schmilzt magisch auf deiner Zunge!")
                )
            ),
            Shop(
                id = "shop_burger_$floor",
                name = "Skyline Burger & Pommes",
                subtitle = "Knusprig, saftig, gigantisch",
                emoji = "🍔",
                shopkeeperName = "Chefkoch Bob",
                shopkeeperAvatar = "👨‍🍳",
                welcomeMessage = "Moin! Direkt vom Grill – frische Pommes und Triple-Cheese Burger!",
                bannerColor = 0xFFFB8500,
                items = listOf(
                    ShopItem("burger_1", "Wolkenkratzer-Burger", "Fünf Lagen saftiges Fleisch mit Schmelzkäse", 45, "🍔", "food", "Reinbeißen!", "Unglaublich lecker! Der beste Burger aller Zeiten!"),
                    ShopItem("burger_2", "Gold-Crunch Pommes", "Extra knusprig mit geheimer Gewürzmischung", 18, "🍟", "food", "Knuspern!", "Knackig und perfekt gesalzen!"),
                    ShopItem("burger_3", "Glitzer-Cola mit Blubber", "Kalt serviert mit Neon-Trinkhalm", 10, "🥤", "food", "Schlürfen!", "Erfrischend kalt und spritzig!")
                )
            )
        )
    }

    private fun createToyShops(floor: Int): List<Shop> {
        return listOf(
            Shop(
                id = "shop_brick_$floor",
                name = "Brick Master Baustein-Tempel",
                subtitle = "Baue alles, was du dir erträumst",
                emoji = "🧱",
                shopkeeperName = "Meister Markus",
                shopkeeperAvatar = "👷",
                welcomeMessage = "Hier gibt es über 10 Millionen bunte Bausteine! Was bauen wir heute?",
                bannerColor = 0xFFE63946,
                items = listOf(
                    ShopItem("brick_1", "Mini-Fahrstuhl Bausatz", "Ein funktionierender Miniatur-Aufzug aus Steinen", 50, "🛗", "toy", "Zusammenbauen!", "Klick-Klick! Der Mini-Aufzug fährt tatsächlich!"),
                    ShopItem("brick_2", "Rennwagen 'Turbo GT'", "Mit echten Gummireifen und Pull-Back Motor", 40, "🏎️", "toy", "Rasen lassen!", "Wrooom! Er schießt quer durch den Raum!"),
                    ShopItem("brick_3", "Drachen-Schloss Set", "Mit leuchtendem Feuerdrachen und Rittern", 85, "🏰", "toy", "Verteidigen!", "Roaaar! Der Drache spuckt Glitzerfeuer!")
                )
            ),
            Shop(
                id = "shop_plush_$floor",
                name = "Kuschel-Insel 'Fluffy Friends'",
                subtitle = "Die weichsten Freunde der Welt",
                emoji = "🧸",
                shopkeeperName = "Lilly Flausch",
                shopkeeperAvatar = "👧",
                welcomeMessage = "Hallo! Drück einmal diesen Bären hier – ist der nicht ultra-weich?",
                bannerColor = 0xFFFF9E00,
                items = listOf(
                    ShopItem("plush_1", "Riesen-Teddybär 'Brummi'", "1 Meter großer Teddy zum Einschlafen", 65, "🧸", "toy", "Fest umarmen!", "Brummi schnurrt und fühlt sich himmlisch an!"),
                    ShopItem("plush_2", "Regenbogen-Einhorn", "Mit glitzerndem Horn und Sternen-Flügeln", 45, "🦄", "toy", "Kuscheln!", "Ein Zauber-Funke erfüllt dein Zimmer!"),
                    ShopItem("plush_3", "Sprechender Hamster", "Wiederholt alles, was du sagst, mit lustiger Stimme", 30, "🐹", "toy", "Sprechen lassen!", "Der Hamster kichert und piepst drauflos!")
                )
            )
        )
    }

    private fun createGamingShops(floor: Int): List<Shop> {
        return listOf(
            Shop(
                id = "shop_arcade_$floor",
                name = "Cyber Pixel Arcade Store",
                subtitle = "Retro-Konsolen, Token & Games",
                emoji = "🕹️",
                shopkeeperName = "Gamer Jack",
                shopkeeperAvatar = "🎧",
                welcomeMessage = "Insert Coin! Du bist im heiligen Gaming-Reich der 100 Etagen gelandet!",
                bannerColor = 0xFF7209B7,
                items = listOf(
                    ShopItem("game_1", "Retro Handheld-Konsole", "Mit 500 vorinstallierten 8-Bit Klassikern", 80, "🎮", "gadget", "Zocken!", "Piep-piep! Du hast einen neuen Highscore geknackt!"),
                    ShopItem("game_2", "VR Hologramm-Brille", "Beame dich in andere Dimensionen", 120, "🥽", "gadget", "Aufsetzen!", "Whow! Um dich herum fliegen 3D-Asteroiden!"),
                    ShopItem("game_3", "Goldener Arcade-Token", "Glücks-Münze für unbegrenzte Freispiele", 25, "🪙", "toy", "Einwerfen!", "EXTRA LIFE! 3 neue Leben freigeschaltet!")
                )
            ),
            Shop(
                id = "shop_manga_$floor",
                name = "Anime & Superhelden Hangar",
                subtitle = "Figuren, Comics & Leuchtschwerter",
                emoji = "🦸‍♂️",
                shopkeeperName = "Sensei Ken",
                shopkeeperAvatar = "🥷",
                welcomeMessage = "Konnichiwa! Finde deine Superhelden-Kräfte hier bei uns!",
                bannerColor = 0xFF3F37C9,
                items = listOf(
                    ShopItem("manga_1", "Neon-Laserschwert", "Leuchtet in Türkis mit realistischem Summen", 75, "⚔️", "toy", "Schwingen!", "Wuuusch-kzzzt! Das Schwert surrt mächtig!"),
                    ShopItem("manga_2", "Superhelden-Umhang", "Lässt dich beim Laufen schneller wirken", 35, "🦸", "wearable", "Umwerfen!", "Der Umhang flattert episch im Fahrstuhl-Wind!"),
                    ShopItem("manga_3", "Sammler-Actionfigur", "Voll beweglich mit geheimen Raketenwerfern", 55, "🤖", "toy", "Posieren!", "Die Figur feuert eine Mini-Glitzerrakete ab!")
                )
            )
        )
    }

    private fun createDinoPetShops(floor: Int): List<Shop> {
        return listOf(
            Shop(
                id = "shop_dino_$floor",
                name = "Jurrassic Dino-Labor",
                subtitle = "Fossilien, Eier & Urzeit-Späße",
                emoji = "🦖",
                shopkeeperName = "Dr. Rexford",
                shopkeeperAvatar = "🦕",
                welcomeMessage = "Vorsicht vor den Zähnen! Unsere Dinos sind frisch geschlüpft und hungrig!",
                bannerColor = 0xFF2B9348,
                items = listOf(
                    ShopItem("dino_1", "Schlüpfendes Dino-Ei", "Lege es ins Wasser, und ein Baby-Raptor schlüpft!", 40, "🥚", "pet", "Ausbrüten!", "Krrk! Die Schale bricht und ein Dino guckt raus!"),
                    ShopItem("dino_2", "Leuchtender T-Rex Zahn", "Echtes Millionen Jahre altes Urzeit-Relikt", 60, "🦷", "magic", "Untersuchen!", "Er vibriert mit uralter Dino-Power!"),
                    ShopItem("dino_3", "Mini-Pterodaktylus Gleiter", "Fliegt ferngesteuert durch die Shopping Mall", 70, "🦅", "toy", "Starten!", "Er kreist majestätisch über den Mall-Etagen!")
                )
            ),
            Shop(
                id = "shop_pet_$floor",
                name = "Pfötchen-Paradies",
                subtitle = "Süße Haustiere & Zubehör",
                emoji = "🐶",
                shopkeeperName = "Pflegerin Mia",
                shopkeeperAvatar = "🐕",
                welcomeMessage = "Wau wau! Schnurr! Alle unsere Tiere suchen einen lieben Spielkameraden!",
                bannerColor = 0xFF55A630,
                items = listOf(
                    ShopItem("pet_1", "Roboter-Welpe 'Sparky'", "Bellt, macht Männchen und liebt Streicheleinheiten", 95, "🐶", "pet", "Streicheln!", "Sparky wedelt mit dem Metallschwanz und bellt fröhlich!"),
                    ShopItem("pet_2", "Kätzchen-Kratzbaum Spielzeug", "Mit Klingelbällen und weichen Federn", 25, "🐱", "toy", "Schütteln!", "Klingeling! Ein kleines Kätzchen springt hinterher!"),
                    ShopItem("pet_3", "Glitzerndes Hundehalsband", "Leuchtet in 7 verschiedenen Farben im Dunkeln", 20, "✨", "wearable", "Anlegen!", "Dein Haustier strahlt wie ein Disco-Star!")
                )
            )
        )
    }

    private fun createSportShops(floor: Int): List<Shop> {
        return listOf(
            Shop(
                id = "shop_skate_$floor",
                name = "Halfpipe Skater Hub",
                subtitle = "Decks, Rollen & Stunt-Helme",
                emoji = "🛹",
                shopkeeperName = "Skater Dave",
                shopkeeperAvatar = "🧢",
                welcomeMessage = "Yo Bro! Schnapp dir ein Board und shredde durch das Einkaufszentrum!",
                bannerColor = 0xFFD90429,
                items = listOf(
                    ShopItem("skate_1", "Custom Neon-Skateboard", "Mit leuchtenden LED-Rollen und Flammen-Grip", 85, "🛹", "toy", "Kickflip machen!", "Klack-ratter! Du landest einen perfekten Ollie!"),
                    ShopItem("skate_2", "Profi-Fingerboard", "Mini-Skateboard für coole Tisch-Stunts", 15, "🛹", "toy", "Fingertrick!", "Ein 360-Shuvit mit deinen Fingern!"),
                    ShopItem("skate_3", "Coole Skater-Snapback Cap", "Mit gesticktem Wolkenkratzer-Logo", 30, "🧢", "wearable", "Aufsetzen!", "Sieht mega lässig aus!")
                )
            ),
            Shop(
                id = "shop_sneaker_$floor",
                name = "Sneaker Vault 'Kicks 100'",
                subtitle = "Limitierte Schuhe mit Air-Glow",
                emoji = "👟",
                shopkeeperName = "Sole Master Jay",
                shopkeeperAvatar = "👟",
                welcomeMessage = "Check die frischesten Kicks! Air-Pads so weich wie Wolken!",
                bannerColor = 0xFFEF233C,
                items = listOf(
                    ShopItem("snk_1", "Hype-Sneaker 'Cloud Jumper'", "Hinterlassen leuchtende Schritte auf dem Mall-Boden", 110, "👟", "wearable", "Anziehen!", "Du federst 2 Meter hoch in die Luft!"),
                    ShopItem("snk_2", "Neon-Schnürsenkel", "Reagieren auf die Musik im Fahrstuhl!", 18, "✨", "wearable", "Einfädeln!", "Sie pulsieren im Takt der Fahrstuhlmusik!"),
                    ShopItem("snk_3", "Sneaker-Schlüsselanhänger", "Miniatur-Kultschuh aus flexiblem Gummi", 12, "🔑", "toy", "Anhängen!", "Dein Schlüsselbund sieht jetzt ultra-hype aus!")
                )
            )
        )
    }

    private fun createRobotShops(floor: Int): List<Shop> {
        return listOf(
            Shop(
                id = "shop_robot_$floor",
                name = "CyberBot Robotics Lab",
                subtitle = "Künstliche Intelligenz zum Mitnehmen",
                emoji = "🤖",
                shopkeeperName = "Robo-Unit 3000",
                shopkeeperAvatar = "🤖",
                welcomeMessage = "BEEP-BOOP! Ich bin programmiert, dir die coolsten Gadgets zu zeigen!",
                bannerColor = 0xFF0077B6,
                items = listOf(
                    ShopItem("bot_1", "Taschen-Drohne 'SkyEye'", "Schwebt automatisch um deinen Kopf und filmt dich", 95, "🛸", "gadget", "Starten!", "Bzzzzz! Die Drohne schwebt sanft durch den Raum!"),
                    ShopItem("bot_2", "Cybernetic Roboter-Arm", "Greift Gegenstände mit hydraulischem Sound", 65, "🦾", "gadget", "Greifen!", "Klack-Zisch! Der Greifer schnappt sich eine Dose!"),
                    ShopItem("bot_3", "Hologramm-Projektor Ball", "Zaubert 3D-Bilder in die Luft", 50, "🔮", "gadget", "Einschalten!", "Ein leuchtender 3D-Fahrstuhl schwebt im Raum!")
                )
            )
        )
    }

    private fun createFunfairShops(floor: Int): List<Shop> {
        return listOf(
            Shop(
                id = "shop_fun_$floor",
                name = "Achterbahn & Kirmes-Souvenirs",
                subtitle = "Adrenalin, Zuckerwatte & Lose",
                emoji = "🎡",
                shopkeeperName = "Zirkusdirektor Toni",
                shopkeeperAvatar = "🎪",
                welcomeMessage = "Treten Sie ein, meine Damen und Herren! Die größte Show über den Wolken!",
                bannerColor = 0xFFFF0054,
                items = listOf(
                    ShopItem("fun_1", "VIP-Achterbahn Ticket", "Einsteigen für die Fahrt über die Dächer der Stadt", 40, "🎟️", "toy", "Fahrt antreten!", "Wuuuuhu! Looping mit 140 km/h im 99. Stock!"),
                    ShopItem("fun_2", "Riesen-Popcorn Eimer", "Karamellisiert und warm mit Buttergeschmack", 22, "🍿", "food", "Knabbern!", "Knusprig-süß! Schmeckt wie im allerbesten Kino!"),
                    ShopItem("fun_3", "Zauberhut mit Wunderstab", "Zieht Kaninchen und Münzen aus dem Nichts!", 65, "🎩", "magic", "Zaubern!", "Abrakadabra! Ein goldener Glitzerregen erscheint!")
                )
            )
        )
    }

    private fun createCosmicShops(floor: Int): List<Shop> {
        return listOf(
            Shop(
                id = "shop_space_$floor",
                name = "Orbit Sky Lounge & Galaxie-Kiosk",
                subtitle = "Das Dach der Welt auf Etage $floor",
                emoji = "🌌",
                shopkeeperName = "Captain Nova",
                shopkeeperAvatar = "👨‍🚀",
                welcomeMessage = "Herzlichen Glückwunsch! Du hast die höchsten Etagen des Wolkenkratzers erreicht!",
                bannerColor = 0xFF480CA8,
                items = listOf(
                    ShopItem("space_1", "Meteoriten-Splitter Souvenir", "Echtes Gestein aus dem Weltall mit Zertifikat", 130, "☄️", "magic", "Bestunen!", "Ein uralter Himmelskörper leuchtet warm in deiner Hand!"),
                    ShopItem("space_2", "Schwerelosigkeits-Drink", "Limonade mit Bläschen, die nach oben schweben", 30, "🧪", "food", "Trinken!", "Whoooa! Du fühlst dich federleicht wie ein Astronaut!"),
                    ShopItem("space_3", "Goldenes Penthouse-Fernglas", "Blicke bis zu den Sternen und den Mondkratern", 90, "🔭", "gadget", "Durchschauen!", "Du siehst eine Raumstation vorbeifliegen!")
                )
            )
        )
    }
}
