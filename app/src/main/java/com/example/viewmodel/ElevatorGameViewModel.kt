package com.example.viewmodel

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ElevatorSoundEngine
import com.example.audio.ElevatorVoiceIntercom
import com.example.model.ElevatorModel
import com.example.model.ElevatorMusicTrack
import com.example.model.MallCatalog
import com.example.model.MallFloor
import com.example.model.Shop
import com.example.model.ShopItem
import com.example.model.SkyscraperFloorRegistry
import com.example.ui.theme.AppThemeMode
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.sign

private const val TAG = "ElevatorGameVM"

sealed interface AssistantAction {
    data class TravelToFloor(val floor: Int, val floorName: String) : AssistantAction
    data object EmergencyRescue : AssistantAction
    data object ToggleMusic : AssistantAction
    data object OpenDoors : AssistantAction
}

enum class ElevatorDirection {
    IDLE, UP, DOWN
}

enum class CabinViewAngle(val label: String, val shortLabel: String, val emoji: String) {
    DOORS("Tür & Mall", "Türseite", "🚪"),
    BUTTON_WALL("Knöpfewand", "Knöpfe", "🔢"),
    GLASS_OUTSIDE("Glas-Ausblick", "Ausblick", "🌆")
}

enum class ElevatorSpeedMode(val label: String, val shortLabel: String, val stepDelayMs: Long, val topSpeedKmh: Int) {
    SLOW_PANORAMA("Panorama-Fahrt 🐢 (Ruhig)", "Ruhig 🐢", 780L, 16),
    NORMAL("Komfort-Tempo 🛗 (Standard)", "Normal 🛗", 480L, 28),
    TURBO("Express-Lift 🚀 (Schnell)", "Schnell 🚀", 210L, 65),
    HYPER_WARP("Warp-Antrieb ⚡ (Turbo)", "Warp ⚡", 85L, 140)
}

enum class CabinAudioChannel {
    HUM, CHIME, MALL_AMBIENCE
}

enum class CabinAudioPreset(
    val label: String,
    val emoji: String,
    val hum: Float,
    val chime: Float,
    val mall: Float
) {
    APPLE_BALANCED("Ausgewogen", "✨", 0.75f, 0.85f, 0.70f),
    CINEMA_IMMERSIVE("Kino-Klang", "🎬", 1.0f, 1.0f, 0.95f),
    ZEN_QUIET("Sanft", "🍃", 0.35f, 0.45f, 0.30f),
    MUTE_ALL("Stumm", "🔇", 0.0f, 0.0f, 0.0f)
}

enum class CabinTheme(val displayName: String, val primaryColor: Long, val accentColor: Long, val darkTone: Long) {
    GOLDEN_LUXURY("Gold & Luxus", 0xFFFFD166, 0xFFFFB703, 0xFF1C1917),
    NEON_CYBERPUNK("Cyberpunk Neon", 0xFF00F0FF, 0xFFFF007F, 0xFF0F172A),
    SPACE_ORBIT("Weltraum Shuttle", 0xFF80ED99, 0xFF57CC99, 0xFF050518),
    RETRO_CLASSIC("Klassischer Holz-Aufzug", 0xFFDDA15E, 0xFFBC6C25, 0xFF28180E)
}

enum class CabinWallMaterial(
    val displayName: String,
    val shortName: String,
    val emoji: String,
    val wallDark: Long,
    val wallLight: Long,
    val doorStart: Long,
    val doorEnd: Long,
    val trimColor: Long
) {
    BRUSHED_STEEL(
        displayName = "Gebürsteter Stahl",
        shortName = "Stahl",
        emoji = "🔩",
        wallDark = 0xFF181E26,
        wallLight = 0xFF323C4A,
        doorStart = 0xFF242C38,
        doorEnd = 0xFF425066,
        trimColor = 0xFF94A3B8
    ),
    CARRARA_MARBLE(
        displayName = "Carrara-Marmor",
        shortName = "Marmor",
        emoji = "🏛️",
        wallDark = 0xFF2A2E37,
        wallLight = 0xFF525A6B,
        doorStart = 0xFF393F4D,
        doorEnd = 0xFF677187,
        trimColor = 0xFFE2E8F0
    ),
    WARM_WALNUT_WOOD(
        displayName = "Edles Walnussholz",
        shortName = "Holz",
        emoji = "🪵",
        wallDark = 0xFF26160C,
        wallLight = 0xFF4A2C18,
        doorStart = 0xFF362011,
        doorEnd = 0xFF5E3A22,
        trimColor = 0xFFDDA15E
    ),
    CHAMPAGNE_GOLD(
        displayName = "Champagner-Gold",
        shortName = "Gold",
        emoji = "✨",
        wallDark = 0xFF282011,
        wallLight = 0xFF4F4023,
        doorStart = 0xFF382D18,
        doorEnd = 0xFF6B5732,
        trimColor = 0xFFFFD166
    ),
    CARBON_TITANIUM(
        displayName = "Carbon & Titan",
        shortName = "Carbon",
        emoji = "🖤",
        wallDark = 0xFF0B0E14,
        wallLight = 0xFF1A202E,
        doorStart = 0xFF121621,
        doorEnd = 0xFF262F45,
        trimColor = 0xFF38BDF8
    )
}

enum class CabinLightingColor(
    val displayName: String,
    val shortName: String,
    val emoji: String,
    val primaryHex: Long,
    val beamHex: Long
) {
    WARM_LOUNGE("Warmweiß Lounge", "Warmweiß", "🕯️", 0xFFFFD166, 0xFFFFFAF0),
    APPLE_ICE_BLUE("Eisblau Studio", "Eisblau", "💎", 0xFF38BDF8, 0xFFE0F2FE),
    CYBER_MAGENTA("Neon-Magenta", "Magenta", "🔮", 0xFFFF007F, 0xFFFCE7F3),
    EMERALD_AURORA("Smaragd-Aurora", "Smaragd", "🌿", 0xFF10B981, 0xFFD1FAE5),
    SUNSET_AMBER("Bernstein-Sonne", "Bernstein", "🌅", 0xFFFF9500, 0xFFFEF3C7)
}

sealed interface AppScreen {
    data object Elevator : AppScreen
    data object MallFloor : AppScreen
    data class ShopDetail(val shop: Shop) : AppScreen
    data object Backpack : AppScreen
}

data class ElevatorGameState(
    val currentFloor: Int = 1,
    val targetFloor: Int = 1,
    val rideStartFloor: Int = 1,
    val isMoving: Boolean = false,
    val doorsOpen: Boolean = true,
    val direction: ElevatorDirection = ElevatorDirection.IDLE,
    val speedKmh: Float = 0f,
    val coins: Int = 300,
    val visitedFloors: Set<Int> = setOf(1),
    val inventory: Map<String, Pair<ShopItem, Int>> = emptyMap(), // id -> (item, count)
    val currentScreen: AppScreen = AppScreen.Elevator,
    val showSettingsDialog: Boolean = false,
    val showDirectoryDialog: Boolean = false,
    val showLegalDialog: Boolean = false,
    val showBonusWheelDialog: Boolean = false,
    val showCoinShopDialog: Boolean = false,
    val musicVolume: Float = 0.7f,
    val sfxVolume: Float = 0.8f,
    val humVolume: Float = 0.75f,
    val chimeVolume: Float = 0.85f,
    val mallAmbienceVolume: Float = 0.70f,
    val showCabinAudioPanel: Boolean = false,
    val speedMode: ElevatorSpeedMode = ElevatorSpeedMode.NORMAL,
    val cabinViewAngle: CabinViewAngle = CabinViewAngle.DOORS,
    val activeItemEffect: ShopItem? = null,
    val activeItemEffectCombo: Int = 0,
    val currentModel: ElevatorModel = ElevatorModel.DEFAULT,
    val showModelSelectDialog: Boolean = false,
    val cabinTheme: CabinTheme = CabinTheme.GOLDEN_LUXURY,
    val wallMaterial: CabinWallMaterial = CabinWallMaterial.BRUSHED_STEEL,
    val lightingColor: CabinLightingColor = CabinLightingColor.WARM_LOUNGE,
    val showCabinCustomizer: Boolean = false,
    val currentMusicTrack: ElevatorMusicTrack = ElevatorMusicTrack.BOSSA_BREEZE,
    val isMusicPlaying: Boolean = false,
    val showMusicPlayerDialog: Boolean = false,
    val showFloorMatrixDialog: Boolean = false,
    val showAssistantDialog: Boolean = false,
    val isEmergencyStuck: Boolean = false,
    val isAssistantStreaming: Boolean = false,
    val isIntercomSpeaking: Boolean = false,
    val isIntercomListening: Boolean = false,
    val isIntercomMuted: Boolean = false,
    val intercomSpokenText: String = "Hier spricht SKY-AI, dein Bordcomputer im SkyMall-Tower. Drücke auf das Mikrofon, um mit mir zu sprechen, oder wähle einen Direkt-Ruf!",
    val intercomStatusBadge: String = "Bordcomputer • gemini-3.5-flash-lite",
    val intercomLastUserSpeech: String? = null,
    val intercomActions: List<AssistantAction> = listOf(
        AssistantAction.TravelToFloor(42, "Cyber Arcade"),
        AssistantAction.TravelToFloor(120, "Sky Deck")
    ),
    val toastMessage: String? = null,
    val lastFountainFloorClaimed: Int? = null,
    val isToddlerMode: Boolean = false,
    val toddlerPin: String = "1234",
    val showToddlerExitDialog: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val useDynamicColor: Boolean = true,
    val hapticsEnabled: Boolean = true
)

class ElevatorGameViewModel(
    private val soundEngine: ElevatorSoundEngine = ElevatorSoundEngine()
) : ViewModel() {

    private val voiceIntercom = ElevatorVoiceIntercom(soundEngine)

    private val intercomSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "spokenReply" to Schema.string(
                    description = "Natürliche, gesprochene Antwort auf Deutsch (2 bis 4 Sätze) für die Sprachausgabe im Fahrstuhl-Intercom."
                ),
                "statusBadge" to Schema.string(
                    description = "Kurzes Status-Label der Durchsage, z.B. 'Notfall-Leitstelle', 'Concierge-Tipp' oder 'Gebäude-Scan'."
                ),
                "recommendedFloor" to Schema.integer(
                    description = "Empfohlene Etage zwischen 1 und 120 für einen Direktfahrt-Button (oder 0, falls keine bestimmte Etage empfohlen wird)."
                ),
                "offerEmergencyRescue" to Schema.boolean(
                    description = "True, wenn der Passagier feststeckt oder die Notentriegelung benötigt."
                ),
                "offerMusicToggle" to Schema.boolean(
                    description = "True, wenn entspannende Fahrstuhlmusik angeboten werden soll."
                )
            )
        )
    }

    private val aiModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.5-flash-lite",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = intercomSchema
            }
        )
    }

    private val conversationMemory = mutableListOf<Pair<String, String>>()

    private val _uiState = MutableStateFlow(ElevatorGameState())
    val uiState: StateFlow<ElevatorGameState> = _uiState.asStateFlow()

    private var rideJob: Job? = null
    private var vibrator: Vibrator? = null

    init {
        viewModelScope.launch {
            voiceIntercom.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isIntercomSpeaking = speaking) }
            }
        }
        viewModelScope.launch {
            voiceIntercom.isListening.collect { listening ->
                _uiState.update { it.copy(isIntercomListening = listening) }
            }
        }
        viewModelScope.launch {
            voiceIntercom.voiceMuted.collect { muted ->
                _uiState.update { it.copy(isIntercomMuted = muted) }
            }
        }
        viewModelScope.launch {
            voiceIntercom.liveTranscript.collect { transcript ->
                if (transcript.isNotBlank()) {
                    _uiState.update { it.copy(intercomLastUserSpeech = transcript) }
                }
            }
        }
    }

    fun initVibrator(context: Context) {
        voiceIntercom.init(context)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (_uiState.value.doorsOpen && !_uiState.value.isMoving) {
            soundEngine.fadeInMallAmbience(_uiState.value.currentFloor)
        }
    }

    private fun triggerHaptic(durationMs: Long = 20, amplitude: Int = 120) {
        if (!_uiState.value.hapticsEnabled) return
        try {
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val safeAmp = amplitude.coerceIn(1, 255)
                    it.vibrate(VibrationEffect.createOneShot(durationMs, safeAmp))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun triggerArrivalChimeHaptic() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0L, 24L, 65L, 42L)
                    val amplitudes = intArrayOf(0, 120, 0, 195)
                    it.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(45L)
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    fun selectTargetFloor(floor: Int) {
        val clamped = floor.coerceIn(1, MallCatalog.TOTAL_FLOORS)
        if (_uiState.value.isMoving) return

        if (clamped != _uiState.value.targetFloor) {
            soundEngine.playButtonClick(600.0 + (clamped % 20) * 20.0)
            triggerHaptic(15)
            _uiState.update { it.copy(targetFloor = clamped) }
        }
    }

    fun adjustTargetFloorBy(delta: Int) {
        if (_uiState.value.isMoving) return
        val currentTarget = _uiState.value.targetFloor
        selectTargetFloor(currentTarget + delta)
    }

    /**
     * Selects target floor and immediately launches the elevator travel ride.
     */
    fun travelToFloor(floor: Int) {
        val clamped = floor.coerceIn(1, MallCatalog.TOTAL_FLOORS)
        if (_uiState.value.isMoving) return
        _uiState.update {
            it.copy(
                isEmergencyStuck = false,
                showAssistantDialog = false,
                showDirectoryDialog = false,
                showFloorMatrixDialog = false
            )
        }
        selectTargetFloor(clamped)
        startElevatorRide()
    }

    /**
     * Starts the elevator travel sequence.
     */
    fun startElevatorRide() {
        val state = _uiState.value
        if (state.isMoving) return

        if (state.targetFloor == state.currentFloor) {
            // Already there! Trigger arrival chime & hum and ensure mall ambience is faded in
            soundEngine.triggerArrivalChimeAndHum(goingUp = true)
            soundEngine.fadeInMallAmbience(state.currentFloor)
            _uiState.update {
                it.copy(
                    doorsOpen = true,
                    isEmergencyStuck = false,
                    showAssistantDialog = false,
                    showDirectoryDialog = false,
                    showFloorMatrixDialog = false,
                    toastMessage = "Du bist bereits auf Etage ${state.currentFloor}!"
                )
            }
            return
        }

        rideJob?.cancel()
        rideJob = viewModelScope.launch {
            // 1. Close doors, fade out mall ambience & step fully inside the elevator cabin
            soundEngine.fadeOutMallAmbience()
            soundEngine.playDoorSlide()
            triggerHaptic(40)
            _uiState.update {
                it.copy(
                    doorsOpen = false,
                    isMoving = true,
                    isEmergencyStuck = false,
                    showAssistantDialog = false,
                    showDirectoryDialog = false,
                    showFloorMatrixDialog = false,
                    rideStartFloor = state.currentFloor,
                    currentScreen = AppScreen.Elevator,
                    direction = if (state.targetFloor > state.currentFloor) ElevatorDirection.UP else ElevatorDirection.DOWN
                )
            }

            delay(600)

            // 2. Start subtle cabin background hum & elevator lounge music while moving!
            soundEngine.startCabinBackgroundHum()
            val trackToPlay = _uiState.value.currentMusicTrack
            soundEngine.startElevatorMusic(trackToPlay)
            _uiState.update { it.copy(isMusicPlaying = true) }

            val target = _uiState.value.targetFloor
            var curr = _uiState.value.currentFloor
            val step = if (target > curr) 1 else -1

            while (curr != target) {
                val activeSpeedMode = _uiState.value.speedMode
                val delayPerFloor = activeSpeedMode.stepDelayMs
                val topSpeed = activeSpeedMode.topSpeedKmh

                curr += step
                val progress = abs(curr - state.currentFloor).toFloat() / abs(target - state.currentFloor).coerceAtLeast(1)
                val currentSpeed = (topSpeed * (0.45f + 0.55f * progress)).coerceIn(8f, topSpeed.toFloat())

                _uiState.update {
                    it.copy(
                        currentFloor = curr,
                        speedKmh = currentSpeed
                    )
                }

                // Play subtle floor pass click sound
                if (delayPerFloor >= 200L || curr % 2 == 0 || curr == target) {
                    soundEngine.playButtonClick(680.0 + (curr % 12) * 15.0)
                    triggerHaptic(10)
                }

                delay(delayPerFloor)
            }

            // 3. Arrived! Stop music and trigger subtle elevator chime + settling cabin hum
            soundEngine.stopElevatorMusic()
            _uiState.update { it.copy(isMusicPlaying = false) }
            delay(120)
            soundEngine.triggerArrivalChimeAndHum(goingUp = step > 0)
            triggerArrivalChimeHaptic()

            val isNewFloor = !state.visitedFloors.contains(target)
            val bonusCoins = if (isNewFloor) 20 else 0
            if (isNewFloor) {
                soundEngine.playKaChing()
            }

            // 4. Open doors & smoothly fade in ambient mall sounds for this floor
            soundEngine.playDoorSlide()
            if (_uiState.value.isToddlerMode) {
                soundEngine.playToddlerChime((target - 1) % 6)
                speakToddlerFloorAnnouncement(target)
            } else {
                soundEngine.fadeInMallAmbience(target)
            }
            _uiState.update {
                it.copy(
                    isMoving = false,
                    doorsOpen = true,
                    direction = ElevatorDirection.IDLE,
                    speedKmh = 0f,
                    visitedFloors = it.visitedFloors + target,
                    coins = it.coins + bonusCoins,
                    toastMessage = if (isNewFloor) "🎉 Neue Etage $target entdeckt! +20 Münzen Belohnung!" else "Angekommen auf Etage $target!"
                )
            }
        }
    }

    fun openDoorsManual() {
        if (_uiState.value.isMoving || _uiState.value.doorsOpen) return
        soundEngine.playMechanicalLatch()
        soundEngine.playDoorSlide()
        soundEngine.fadeInMallAmbience(_uiState.value.currentFloor)
        triggerHaptic(25)
        _uiState.update { it.copy(doorsOpen = true, isEmergencyStuck = false) }
    }

    fun closeDoorsManual() {
        if (_uiState.value.isMoving || !_uiState.value.doorsOpen) return
        soundEngine.playMechanicalLatch()
        soundEngine.playDoorSlide()
        soundEngine.fadeOutMallAmbience()
        triggerHaptic(25)
        _uiState.update { it.copy(doorsOpen = false) }
    }

    fun ringEmergencyBell() {
        soundEngine.playAlarmBell()
        triggerHaptic(80)
        val state = _uiState.value
        val currentFloorData = SkyscraperFloorRegistry.getFloor(state.currentFloor)
        val greeting = when {
            state.isEmergencyStuck -> {
                "Notruf-Leitstelle SKY-AI hier! Wir stehen zwischen Etage ${state.currentFloor} und ${(state.currentFloor + 1).coerceAtMost(120)}, aber die Magnetbremse hält bombenfest. Sag mir Bescheid oder drücke auf Not-Entriegelung, dann hole ich dich sofort hier raus!"
            }
            state.isMoving -> {
                "Hier spricht Bordcomputer SKY-AI! Wir gleiten gerade mit ${state.speedKmh.toInt()} Stundenkilometern an Etage ${state.currentFloor}, ${currentFloorData.name}, vorbei. Kann ich dir bei der Fahrt helfen oder dir ein Etagen-Highlight zeigen?"
            }
            else -> {
                "Hallo! Hier spricht SKY-AI, dein Bordcomputer auf Etage ${state.currentFloor}, ${currentFloorData.name}. Drücke aufs Mikrofon und sprich mit mir – egal ob du feststeckst oder die besten Etagen im Wolkenkratzer suchst!"
            }
        }
        val actions = if (state.isEmergencyStuck) {
            listOf(AssistantAction.EmergencyRescue, AssistantAction.ToggleMusic)
        } else {
            listOf(
                AssistantAction.TravelToFloor(42, "Cyber Arcade"),
                AssistantAction.TravelToFloor(50, "Dino-Dschungel"),
                AssistantAction.TravelToFloor(120, "Sky Deck")
            )
        }
        _uiState.update {
            it.copy(
                showAssistantDialog = true,
                intercomSpokenText = greeting,
                intercomStatusBadge = if (state.isMoving) "Live-Funk • In Fahrt" else "Intercom Verbunden",
                intercomActions = actions
            )
        }
        voiceIntercom.speak(greeting)
    }

    fun toggleAssistantDialog(show: Boolean) {
        soundEngine.playButtonClick()
        if (!show) {
            voiceIntercom.stopSpeaking()
            voiceIntercom.stopListening()
        }
        _uiState.update { it.copy(showAssistantDialog = show) }
    }

    fun toggleIntercomMute() {
        val muted = voiceIntercom.toggleMute()
        soundEngine.playButtonClick(if (muted) 440.0 else 880.0)
    }

    fun replayIntercomSpeech() {
        val text = _uiState.value.intercomSpokenText
        if (text.isNotBlank()) {
            voiceIntercom.speak(text)
        }
    }

    fun startVoiceListening(context: Context) {
        triggerHaptic(30)
        voiceIntercom.startListening(
            context = context,
            onFinalResult = { recognizedSpeech ->
                sendAssistantMessage(recognizedSpeech)
            },
            onErrorMessage = { errorMsg ->
                _uiState.update {
                    it.copy(
                        intercomSpokenText = errorMsg,
                        intercomStatusBadge = "Audio-Hinweis"
                    )
                }
                voiceIntercom.speak(errorMsg)
            }
        )
    }

    fun stopVoiceListening() {
        voiceIntercom.stopListening()
    }

    fun simulateEmergencyStuck() {
        rideJob?.cancel()
        soundEngine.stopElevatorMusic()
        soundEngine.fadeOutMallAmbience()
        soundEngine.playAlarmBell()
        triggerHaptic(120)
        val floor = _uiState.value.currentFloor
        val floorData = SkyscraperFloorRegistry.getFloor(floor)
        val stuckSpeech = "Achtung! Notbremse zwischen Etage $floor, ${floorData.name}, und Etage ${(floor + 1).coerceAtMost(120)} aktiviert! Meine Diagnose zeigt: Alle acht Titan-Tragseile sind zu hundert Prozent stabil und der Kabinen-Sauerstoff liegt bei entspannten einundzwanzig Prozent. Sag einfach Befreien oder drücke auf Not-Entriegelung, damit ich die Kabine entriegele!"
        _uiState.update {
            it.copy(
                isMoving = false,
                doorsOpen = false,
                isEmergencyStuck = true,
                direction = ElevatorDirection.IDLE,
                speedKmh = 0f,
                showAssistantDialog = true,
                intercomSpokenText = stuckSpeech,
                intercomStatusBadge = "🚨 Notfall-Protokoll Aktiv",
                intercomActions = listOf(
                    AssistantAction.EmergencyRescue,
                    AssistantAction.ToggleMusic
                ),
                toastMessage = "🚨 Notfall-Simulation: Fahrstuhl steckt fest!"
            )
        }
        voiceIntercom.speak(stuckSpeech)
    }

    fun performEmergencyRescue() {
        soundEngine.playMechanicalLatch()
        soundEngine.triggerArrivalChimeAndHum(goingUp = true)
        soundEngine.playDoorSlide()
        val floor = _uiState.value.currentFloor
        soundEngine.fadeInMallAmbience(floor)
        triggerHaptic(90)
        val floorData = SkyscraperFloorRegistry.getFloor(floor)
        val rescueSpeech = "Not-Entriegelung erfolgreich abgeschlossen! Ich habe den Sicherheitsriegel per Magnet-Impuls gelöst und die Kabine sanft auf Etage $floor, ${floorData.name}, nivelliert. Die Türen sind wieder weit geöffnet – und für deine Ruhe im Notfall schenke ich dir fünfundzwanzig Tapferkeits-Münzen!"
        _uiState.update {
            it.copy(
                isEmergencyStuck = false,
                isMoving = false,
                doorsOpen = true,
                coins = it.coins + 25,
                intercomSpokenText = rescueSpeech,
                intercomStatusBadge = "✅ Rettung Abgeschlossen",
                intercomActions = listOf(
                    AssistantAction.TravelToFloor(50, "Dino-Dschungel"),
                    AssistantAction.TravelToFloor(100, "Jubiläums-Gala")
                ),
                toastMessage = "🔓 Kabine befreit! +25 Tapferkeits-Münzen!"
            )
        }
        voiceIntercom.speak(rescueSpeech)
    }

    fun sendAssistantMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _uiState.value.isAssistantStreaming) return

        voiceIntercom.stopSpeaking()
        soundEngine.playButtonClick(820.0)
        triggerHaptic(20)

        val stateSnapshot = _uiState.value
        val fallbackActions = buildSmartActionsForQuery(trimmed, stateSnapshot)

        _uiState.update {
            it.copy(
                intercomLastUserSpeech = trimmed,
                intercomSpokenText = "Bordcomputer SKY-AI verarbeitet deine Anfrage…",
                intercomStatusBadge = "⚡ gemini-3.5-flash-lite aktiv…",
                isAssistantStreaming = true
            )
        }

        viewModelScope.launch {
            val currentFloorInfo = SkyscraperFloorRegistry.getFloor(stateSnapshot.currentFloor)
            val currentZone = MallCatalog.getZoneForFloor(stateSnapshot.currentFloor)
            val currentShops = MallCatalog.getFloor(stateSnapshot.currentFloor).shops.joinToString(", ") {
                "${it.emoji} ${it.name} (${it.subtitle})"
            }
            val recentHistoryText = if (conversationMemory.isEmpty()) {
                "Noch kein vorheriger Dialog."
            } else {
                conversationMemory.takeLast(4).joinToString("\n") { (u, a) ->
                    "Passagier: $u\nSKY-AI: $a"
                }
            }

            val prompt = """
                Du bist SKY-AI, der sprechende Bordcomputer und Gegensprechanlagen-Assistent im 120-stöckigen „SkyMall Elevator“ Wolkenkratzer.
                Dein Charakter ist eine souveräne Kombination aus:
                1) Einem ruhigen, aufmerksamen SkyMall Studio-Concierge & Bordcomputer
                2) Einem freundlichen, humorvollen futuristischen KI-Begleiter mit Charme.
                
                WICHTIG: Deine Antwort in `spokenReply` wird direkt über die deutsche Sprachausgabe (Text-to-Speech) der Aufzug-Gegensprechanlage laut vorgelesen!
                Formuliere `spokenReply` deshalb wie echte, lebendige gesprochene Sprache auf Deutsch (ohne Markdown-Sternchen, maximal 2 bis 4 prägnante Sätze).
                
                Aktueller Live-Standort & Zustand des Aufzugs:
                - Aktuelle Etage: Etage ${stateSnapshot.currentFloor} (${currentFloorInfo.name} – ${currentFloorInfo.tagline}, Höhe: ${currentFloorInfo.altitudeMeters} Meter)
                - Aktuelle Themen-Zone: ${currentZone.title} (${currentZone.description})
                - Geschäfte direkt auf dieser Etage ${stateSnapshot.currentFloor}: $currentShops
                - Hat Etage ${stateSnapshot.currentFloor} einen Wunschbrunnen für Gratis-Münzen?: ${if (currentFloorInfo.hasCoinFountain) "Ja!" else "Nein"}
                - Ziel-Etage: Etage ${stateSnapshot.targetFloor}
                - Türen geöffnet: ${stateSnapshot.doorsOpen}
                - Kabine steckt im Notfall fest: ${stateSnapshot.isEmergencyStuck}
                - Aufzug-Modell: ${stateSnapshot.currentModel.modelName} (${stateSnapshot.currentModel.topSpeedKmh} km/h Spitze, max. ${stateSnapshot.currentModel.maxPassengers} Personen)
                - Münzstand des Passagiers: ${stateSnapshot.coins} Münzen
                
                Berühmte Highlight-Etagen im 120-stöckigen SkyMall-Gebäude:
                - Etage 1: Süßigkeiten-Lobby (Candy Wonderland)
                - Etage 10: Schokoladen-Fabrik
                - Etage 25: Baustein-Metropole (Klötzchen- & Spielzeugwelt)
                - Etage 42: Cyber-Arcade & VR-Gaming-Arena
                - Etage 50: Urzeit-Dino-Dschungel mit T-Rex
                - Etage 69: Neon-Skaterpark
                - Etage 77: KI- & Roboter-Forschungslabor
                - Etage 100: Goldene Jubiläums-Gala & VIP-Lounge
                - Etage 120: Stratosphären-Sky-Deck & Sternwarte in 480 Metern Höhe
                
                Bisheriger kurzer Gesprächskontext:
                $recentHistoryText
                
                Aktuelle Spracheingabe des Passagiers über das Aufzug-Mikrofon:
                "$trimmed"
            """.trimIndent()

            try {
                val response = withTimeout(35_000L) {
                    aiModel.generateContent(prompt)
                }
                val rawJson = response.text?.trim().orEmpty()
                if (rawJson.isEmpty()) {
                    throw IllegalStateException("Empty response from gemini-3.5-flash-lite")
                }

                val json = JSONObject(rawJson)
                val spokenReply = json.optString("spokenReply").trim().ifEmpty {
                    buildLocalConciergeReply(trimmed, stateSnapshot)
                }
                val badge = json.optString("statusBadge", "Gemini 3.5 Flash-Lite").trim()
                val recFloor = json.optInt("recommendedFloor", 0)
                val offerRescue = json.optBoolean("offerEmergencyRescue", false)
                val offerMusic = json.optBoolean("offerMusicToggle", false)

                val dynamicActions = mutableListOf<AssistantAction>()
                if (offerRescue || stateSnapshot.isEmergencyStuck) {
                    dynamicActions.add(AssistantAction.EmergencyRescue)
                }
                if (recFloor in 1..MallCatalog.TOTAL_FLOORS && recFloor != stateSnapshot.currentFloor) {
                    val flData = SkyscraperFloorRegistry.getFloor(recFloor)
                    dynamicActions.add(AssistantAction.TravelToFloor(recFloor, flData.name))
                }
                if (offerMusic) {
                    dynamicActions.add(AssistantAction.ToggleMusic)
                }
                val finalActions = extractFloorActionsFromReply(
                    spokenReply,
                    if (dynamicActions.isNotEmpty()) dynamicActions else fallbackActions,
                    stateSnapshot
                )

                conversationMemory.add(trimmed to spokenReply)

                _uiState.update {
                    it.copy(
                        isAssistantStreaming = false,
                        intercomSpokenText = spokenReply,
                        intercomStatusBadge = "$badge • Gemini KI",
                        intercomActions = finalActions
                    )
                }
                voiceIntercom.speak(spokenReply)
            } catch (e: CancellationException) {
                throw e
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "gemini-3.5-flash-lite timed out", e)
                val fallback = buildLocalConciergeReply(trimmed, stateSnapshot)
                _uiState.update {
                    it.copy(
                        isAssistantStreaming = false,
                        intercomSpokenText = fallback,
                        intercomStatusBadge = "Bord-Diagnose • Direktfunk",
                        intercomActions = fallbackActions
                    )
                }
                voiceIntercom.speak(fallback)
            } catch (e: Exception) {
                Log.e(TAG, "gemini-3.5-flash-lite call failed, using onboard voice fallback", e)
                val fallback = buildLocalConciergeReply(trimmed, stateSnapshot)
                _uiState.update {
                    it.copy(
                        isAssistantStreaming = false,
                        intercomSpokenText = fallback,
                        intercomStatusBadge = "Bord-Diagnose • Intern",
                        intercomActions = fallbackActions
                    )
                }
                voiceIntercom.speak(fallback)
            }
        }
    }

    private fun buildSmartActionsForQuery(query: String, state: ElevatorGameState): List<AssistantAction> {
        val q = query.lowercase()
        val actions = mutableListOf<AssistantAction>()

        if (state.isEmergencyStuck || q.contains("steck") || q.contains("hilfe") || q.contains("notfall") || q.contains("fest") || q.contains("tür")) {
            actions.add(AssistantAction.EmergencyRescue)
            if (!state.doorsOpen) {
                actions.add(AssistantAction.OpenDoors)
            }
            actions.add(AssistantAction.ToggleMusic)
        }

        val numberRegex = Regex("\\b([1-9][0-9]?|1[01][0-9]|120)\\b")
        numberRegex.findAll(q).forEach { match ->
            val fl = match.value.toIntOrNull()
            if (fl != null && fl in 1..MallCatalog.TOTAL_FLOORS && fl != state.currentFloor) {
                val flData = SkyscraperFloorRegistry.getFloor(fl)
                actions.add(AssistantAction.TravelToFloor(fl, flData.name))
            }
        }

        if (q.contains("musik") || q.contains("song") || q.contains("lounge")) {
            actions.add(AssistantAction.ToggleMusic)
        }

        if (actions.isEmpty()) {
            actions.add(AssistantAction.TravelToFloor(42, "Cyber Arcade"))
            actions.add(AssistantAction.TravelToFloor(50, "Dino-Dschungel"))
            actions.add(AssistantAction.TravelToFloor(120, "Sky Deck"))
        }

        return actions.distinct().take(3)
    }

    private fun extractFloorActionsFromReply(
        replyText: String,
        existingActions: List<AssistantAction>,
        state: ElevatorGameState
    ): List<AssistantAction> {
        val combined = existingActions.toMutableList()
        val floorRegex = Regex("Etage\\s+(\\d{1,3})", RegexOption.IGNORE_CASE)
        floorRegex.findAll(replyText).forEach { m ->
            val fl = m.groupValues[1].toIntOrNull()
            if (fl != null && fl in 1..MallCatalog.TOTAL_FLOORS && fl != state.currentFloor) {
                val flData = SkyscraperFloorRegistry.getFloor(fl)
                val action = AssistantAction.TravelToFloor(fl, flData.name)
                if (combined.none { it is AssistantAction.TravelToFloor && it.floor == fl }) {
                    combined.add(0, action)
                }
            }
        }
        return combined.distinct().take(3)
    }

    private fun buildLocalConciergeReply(query: String, state: ElevatorGameState): String {
        val q = query.lowercase()
        val currData = SkyscraperFloorRegistry.getFloor(state.currentFloor)
        val currZone = MallCatalog.getZoneForFloor(state.currentFloor)

        return when {
            q.contains("steck") || q.contains("fest") || q.contains("hilfe") || q.contains("notfall") -> {
                "Keine Panik, Passagier! Meine Quanten-Sensoren melden: Unsere acht Titan-Tragseile halten selbst einen ausgewachsenen T-Rex aus Etage 50 problemlos aus. Tippe direkt unter mir auf Not-Entriegelung, dann öffne ich sofort die Kabinentüren auf Etage ${state.currentFloor}!"
            }
            q.contains("münz") || q.contains("geld") || q.contains("coin") || q.contains("brunnen") -> {
                "Du hast aktuell ${state.coins} Münzen auf dem Konto! Jede neu entdeckte Etage bringt dir sofort zwanzig Bonus-Münzen, auf vielen Etagen sprudeln goldene Wunschbrunnen, und in den Einstellungen oben rechts kannst du dir jederzeit kostenlos zweihundertfünfzig Münzen Taschengeld abholen."
            }
            q.contains("geheim") || q.contains("gebäude") || q.contains("wolkenkratzer") -> {
                "Architektur-Geheimnis freigeschaltet: Der SkyMall-Tower ist vierhundertachtzig Meter hoch und besitzt hundertzwanzig klimatisierte Erlebnis-Etagen. Auf Etage 77 entwickeln meine Roboter-Kollegen geheime Prototypen, und von Etage 120 blickst du direkt in die Stratosphäre!"
            }
            q.contains("musik") || q.contains("song") -> {
                "Aktuell ist ${state.currentMusicTrack.title} in der Kabinen-Anlage geladen. Klicke einfach unten auf den Musik-Button, damit ich den Bord-Subwoofer für dich einschalte!"
            }
            q.contains("aktuell") || q.contains("hier") || q.contains("wo bin ich") || q.contains("etage ${state.currentFloor}") -> {
                "Wir befinden uns gerade auf Etage ${state.currentFloor}, ${currData.name}, in ${currData.altitudeMeters} Metern Höhe in der Zone ${currZone.title}. Das Motto hier lautet: ${currData.tagline}."
            }
            else -> {
                "Bordcomputer SKY-AI meldet von Etage ${state.currentFloor}, ${currData.name}: Alle Systeme laufen mit höchster Präzision! Wenn du Action suchst, empfehle ich dir Etage 42 mit der Cyber Arcade, Etage 50 mit dem Dino-Dschungel oder Etage 120 auf dem Sky Deck."
            }
        }
    }

    fun playButtonClick(freqHz: Double = 880.0) {
        soundEngine.playButtonClick(freqHz)
        triggerHaptic(15)
    }

    fun stepIntoMall() {
        if (_uiState.value.isMoving) return
        soundEngine.playDoorSlide()
        triggerHaptic(20)
        _uiState.update { it.copy(currentScreen = AppScreen.MallFloor) }
    }

    fun returnToElevator() {
        soundEngine.playButtonClick()
        triggerHaptic(20)
        _uiState.update { it.copy(currentScreen = AppScreen.Elevator) }
    }

    fun enterShop(shop: Shop) {
        soundEngine.playButtonClick()
        triggerHaptic(20)
        _uiState.update { it.copy(currentScreen = AppScreen.ShopDetail(shop)) }
    }

    fun leaveShop() {
        soundEngine.playButtonClick()
        triggerHaptic(20)
        _uiState.update { it.copy(currentScreen = AppScreen.MallFloor) }
    }

    fun openBackpack() {
        soundEngine.playButtonClick()
        triggerHaptic(20)
        _uiState.update { it.copy(currentScreen = AppScreen.Backpack) }
    }

    fun closeBackpack() {
        soundEngine.playButtonClick()
        triggerHaptic(20)
        _uiState.update { it.copy(currentScreen = AppScreen.Elevator) }
    }

    fun buyItem(item: ShopItem) {
        val state = _uiState.value
        if (state.coins < item.price) {
            soundEngine.playButtonClick(300.0)
            _uiState.update { it.copy(toastMessage = "Nicht genügend Münzen! Hol dir Taschengeld in den Einstellungen.") }
            return
        }

        soundEngine.playKaChing()
        triggerHaptic(50)

        val updatedInventory = state.inventory.toMutableMap()
        val currentCount = updatedInventory[item.id]?.second ?: 0
        updatedInventory[item.id] = Pair(item, currentCount + 1)

        _uiState.update {
            it.copy(
                coins = it.coins - item.price,
                inventory = updatedInventory,
                toastMessage = "Gekauft: ${item.name}! Ab jetzt in deinem Rucksack."
            )
        }
    }

    fun useItem(item: ShopItem) {
        val soundType = when (item.category) {
            "food" -> "eat"
            "toy" -> "toy"
            "magic" -> "magic"
            else -> "default"
        }
        soundEngine.playItemAction(soundType)
        triggerHaptic(40)
        _uiState.update {
            val nextCombo = if (it.activeItemEffect?.id == item.id) it.activeItemEffectCombo + 1 else 1
            it.copy(
                activeItemEffect = item,
                activeItemEffectCombo = nextCombo,
                toastMessage = "${item.emoji} ${item.actionFeedback}"
            )
        }
    }

    fun dismissItemEffect() {
        soundEngine.playButtonClick(640.0)
        _uiState.update { it.copy(activeItemEffect = null, activeItemEffectCombo = 0) }
    }

    fun selectCabinViewAngle(angle: CabinViewAngle) {
        if (_uiState.value.cabinViewAngle == angle) return
        soundEngine.playButtonClick(760.0)
        triggerHaptic(18)
        _uiState.update { it.copy(cabinViewAngle = angle) }
    }

    fun cycleCabinViewAngle(delta: Int) {
        val entries = CabinViewAngle.entries
        val currentIdx = entries.indexOf(_uiState.value.cabinViewAngle)
        val nextIdx = (currentIdx + delta).mod(entries.size)
        selectCabinViewAngle(entries[nextIdx])
    }

    fun claimFountainCoins() {
        val floor = _uiState.value.currentFloor
        if (_uiState.value.lastFountainFloorClaimed == floor) {
            _uiState.update { it.copy(toastMessage = "Brunnen-Wunsch für heute schon eingelöst! Fahr auf eine andere Etage!") }
            return
        }

        val bonus = (15..45).random()
        soundEngine.playKaChing()
        triggerHaptic(40)
        _uiState.update {
            it.copy(
                coins = it.coins + bonus,
                lastFountainFloorClaimed = floor,
                toastMessage = "✨ Münzbrunnen Glück: +$bonus glänzende Münzen!"
            )
        }
    }

    fun addPocketMoney(amount: Int = 250) {
        soundEngine.playKaChing()
        triggerHaptic(40)
        _uiState.update {
            it.copy(
                coins = it.coins + amount,
                toastMessage = "💰 Taschengeld erhalten: +$amount Münzen!"
            )
        }
    }

    fun toggleSettingsDialog(show: Boolean) {
        soundEngine.playButtonClick()
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun toggleLegalDialog(show: Boolean) {
        soundEngine.playButtonClick()
        _uiState.update { it.copy(showLegalDialog = show) }
    }

    fun resetAllGameData() {
        soundEngine.playElevatorChime()
        _uiState.update {
            ElevatorGameState(
                toastMessage = "Spielstand wurde zurückgesetzt.",
                showSettingsDialog = false,
                showLegalDialog = false
            )
        }
    }

    fun toggleDirectoryDialog(show: Boolean) {
        soundEngine.playButtonClick()
        _uiState.update { it.copy(showDirectoryDialog = show) }
    }

    fun toggleCoinShopDialog(show: Boolean) {
        soundEngine.playButtonClick()
        _uiState.update { it.copy(showCoinShopDialog = show) }
    }

    fun creditPurchasedCoins(amount: Int, productId: String) {
        soundEngine.playKaChing()
        triggerHaptic(60)
        _uiState.update {
            it.copy(
                coins = it.coins + amount,
                toastMessage = "🪙 +$amount Münzen gutgeschrieben! ($productId)"
            )
        }
    }

    fun nextMusicTrack() {
        val next = _uiState.value.currentMusicTrack.next()
        selectMusicTrack(next)
    }

    fun previousMusicTrack() {
        val prev = _uiState.value.currentMusicTrack.previous()
        selectMusicTrack(prev)
    }

    fun selectMusicTrack(track: ElevatorMusicTrack) {
        soundEngine.playButtonClick(880.0)
        triggerHaptic(15)
        _uiState.update {
            it.copy(
                currentMusicTrack = track,
                toastMessage = "🎵 Fahrstuhl-Musik: ${track.emoji} ${track.title}"
            )
        }
        if (_uiState.value.isMusicPlaying) {
            soundEngine.startElevatorMusic(track)
        }
    }

    fun toggleMusicPlayPause() {
        val currentlyPlaying = _uiState.value.isMusicPlaying
        soundEngine.playButtonClick(750.0)
        triggerHaptic(20)
        if (currentlyPlaying) {
            soundEngine.stopElevatorMusic()
            _uiState.update {
                it.copy(
                    isMusicPlaying = false,
                    toastMessage = "Fahrstuhl-Musik pausiert ⏸️"
                )
            }
        } else {
            soundEngine.startElevatorMusic(_uiState.value.currentMusicTrack)
            _uiState.update {
                it.copy(
                    isMusicPlaying = true,
                    toastMessage = "Fahrstuhl-Musik spielt: ${_uiState.value.currentMusicTrack.emoji} ${_uiState.value.currentMusicTrack.title} ▶️"
                )
            }
        }
    }

    fun toggleMusicPlayerDialog(show: Boolean) {
        soundEngine.playButtonClick()
        _uiState.update { it.copy(showMusicPlayerDialog = show) }
    }

    fun toggleFloorMatrixDialog(show: Boolean) {
        soundEngine.playButtonClick()
        _uiState.update { it.copy(showFloorMatrixDialog = show) }
    }

    fun updateMusicVolume(volume: Float) {
        soundEngine.musicVolume = volume
        _uiState.update { it.copy(musicVolume = volume) }
    }

    fun updateSfxVolume(volume: Float) {
        soundEngine.sfxVolume = volume
        _uiState.update { it.copy(sfxVolume = volume) }
    }

    fun updateHumVolume(volume: Float, playPreview: Boolean = false) {
        val clamped = volume.coerceIn(0f, 1f)
        soundEngine.humVolume = clamped
        _uiState.update { it.copy(humVolume = clamped) }
        if (playPreview && clamped > 0.02f) {
            soundEngine.playHumPreview()
        }
    }

    fun updateChimeVolume(volume: Float, playPreview: Boolean = false) {
        val clamped = volume.coerceIn(0f, 1f)
        soundEngine.chimeVolume = clamped
        _uiState.update { it.copy(chimeVolume = clamped) }
        if (playPreview && clamped > 0.02f) {
            soundEngine.playChimePreview()
        }
    }

    fun updateMallAmbienceVolume(volume: Float, playPreview: Boolean = false) {
        val clamped = volume.coerceIn(0f, 1f)
        soundEngine.mallAmbienceVolume = clamped
        _uiState.update { it.copy(mallAmbienceVolume = clamped) }
        if (playPreview && clamped > 0.02f) {
            soundEngine.playMallAmbiencePreview(_uiState.value.currentFloor)
        }
    }

    fun applyCabinAudioPreset(preset: CabinAudioPreset) {
        soundEngine.humVolume = preset.hum
        soundEngine.chimeVolume = preset.chime
        soundEngine.mallAmbienceVolume = preset.mall
        triggerHaptic(20)
        _uiState.update {
            it.copy(
                humVolume = preset.hum,
                chimeVolume = preset.chime,
                mallAmbienceVolume = preset.mall,
                toastMessage = "🎚️ Kabinen-Klangprofil: ${preset.emoji} ${preset.label}"
            )
        }
        if (preset.chime > 0.02f) {
            soundEngine.playChimePreview()
        }
    }

    fun previewCabinAudioChannel(channel: CabinAudioChannel) {
        triggerHaptic(18)
        when (channel) {
            CabinAudioChannel.HUM -> {
                if (_uiState.value.humVolume <= 0.02f) {
                    updateHumVolume(0.75f, playPreview = true)
                } else {
                    soundEngine.playHumPreview()
                }
            }
            CabinAudioChannel.CHIME -> {
                if (_uiState.value.chimeVolume <= 0.02f) {
                    updateChimeVolume(0.85f, playPreview = true)
                } else {
                    soundEngine.playChimePreview()
                }
            }
            CabinAudioChannel.MALL_AMBIENCE -> {
                if (_uiState.value.mallAmbienceVolume <= 0.02f) {
                    updateMallAmbienceVolume(0.70f, playPreview = true)
                } else {
                    soundEngine.playMallAmbiencePreview(_uiState.value.currentFloor)
                }
            }
        }
    }

    fun toggleCabinAudioPanel(show: Boolean? = null) {
        soundEngine.playButtonClick(780.0)
        triggerHaptic(15)
        _uiState.update {
            val next = show ?: !it.showCabinAudioPanel
            it.copy(
                showCabinAudioPanel = next,
                showCabinCustomizer = if (next) false else it.showCabinCustomizer
            )
        }
    }

    fun toggleCabinCustomizer(show: Boolean? = null) {
        soundEngine.playButtonClick(820.0)
        triggerHaptic(15)
        _uiState.update {
            val next = show ?: !it.showCabinCustomizer
            it.copy(
                showCabinCustomizer = next,
                showCabinAudioPanel = if (next) false else it.showCabinAudioPanel
            )
        }
    }

    fun updateWallMaterial(material: CabinWallMaterial) {
        soundEngine.playMechanicalLatch()
        triggerHaptic(25)
        _uiState.update {
            it.copy(
                wallMaterial = material,
                toastMessage = "🎨 Kabinen-Material: ${material.emoji} ${material.displayName}"
            )
        }
    }

    fun updateLightingColor(lighting: CabinLightingColor) {
        soundEngine.playButtonClick(920.0)
        triggerHaptic(20)
        _uiState.update {
            it.copy(
                lightingColor = lighting,
                toastMessage = "💡 Kabinen-Beleuchtung: ${lighting.emoji} ${lighting.displayName}"
            )
        }
    }

    fun updateSpeedMode(mode: ElevatorSpeedMode) {
        soundEngine.playButtonClick(840.0)
        triggerHaptic(20)
        _uiState.update {
            it.copy(
                speedMode = mode,
                toastMessage = "🛗 Fahrstuhl-Tempo: ${mode.shortLabel} (bis ${mode.topSpeedKmh} km/h)"
            )
        }
    }

    fun updateCabinTheme(theme: CabinTheme) {
        soundEngine.playButtonClick()
        triggerHaptic(18, 100)
        _uiState.update { it.copy(cabinTheme = theme) }
    }

    fun updateThemeMode(mode: AppThemeMode) {
        soundEngine.playButtonClick(820.0)
        triggerHaptic(18, 105)
        _uiState.update {
            it.copy(
                themeMode = mode,
                toastMessage = "${mode.emoji} Erscheinungsbild: ${mode.label}"
            )
        }
    }

    fun toggleDynamicColor(enabled: Boolean) {
        soundEngine.playButtonClick(860.0)
        triggerHaptic(18, 105)
        _uiState.update {
            it.copy(
                useDynamicColor = enabled,
                toastMessage = if (enabled) "🎨 Dynamic Color aktiviert" else "🎨 Studio-Farbpalette aktiv"
            )
        }
    }

    fun toggleHaptics(enabled: Boolean) {
        soundEngine.playButtonClick(780.0)
        _uiState.update {
            it.copy(
                hapticsEnabled = enabled,
                toastMessage = if (enabled) "📳 Taktile Haptik aktiviert" else "Haptik deaktiviert"
            )
        }
        if (enabled) {
            triggerHaptic(22, 130)
        }
    }

    fun selectElevatorModel(model: ElevatorModel) {
        soundEngine.playMechanicalLatch()
        soundEngine.playElevatorChime()
        triggerHaptic(35)
        _uiState.update {
            it.copy(
                currentModel = model,
                cabinTheme = model.cabinTheme,
                showModelSelectDialog = false,
                toastMessage = "🛗 Aufzug gewechselt: ${model.emoji} ${model.modelName} (${model.topSpeedKmh} km/h • ${model.maxPassengers} Pers.)"
            )
        }
    }

    fun toggleModelSelectDialog(show: Boolean) {
        soundEngine.playButtonClick()
        _uiState.update { it.copy(showModelSelectDialog = show) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun enableToddlerMode() {
        soundEngine.playToddlerChime(2)
        _uiState.update {
            it.copy(
                isToddlerMode = true,
                showSettingsDialog = false,
                showCabinAudioPanel = false,
                showCabinCustomizer = false,
                showDirectoryDialog = false,
                showFloorMatrixDialog = false,
                showAssistantDialog = false,
                showModelSelectDialog = false,
                showMusicPlayerDialog = false,
                showCoinShopDialog = false,
                showLegalDialog = false,
                showToddlerExitDialog = false,
                toastMessage = "🧸 Kleinkind-Modus aktiv: Reizarm & sicher!"
            )
        }
        voiceIntercom.speak("Hallo! Willkommen im Zauber-Fahrstuhl. Drücke einfach auf eine bunte Zahl!")
    }

    fun disableToddlerMode() {
        soundEngine.playElevatorChime()
        _uiState.update {
            it.copy(
                isToddlerMode = false,
                showToddlerExitDialog = false,
                toastMessage = "Normaler Fahrstuhl-Modus wieder aktiv."
            )
        }
    }

    fun toggleToddlerExitDialog(show: Boolean) {
        soundEngine.playButtonClick()
        _uiState.update { it.copy(showToddlerExitDialog = show) }
    }

    fun playToddlerButtonTap(floor: Int) {
        soundEngine.playToddlerPop(440.0 + (floor % 10) * 45.0)
        triggerHaptic(15)
        speakToddlerButtonTap(floor)
    }

    private fun speakToddlerButtonTap(floor: Int) {
        val numberWord = when (floor) {
            1 -> "Etage eins!"
            2 -> "Etage zwei!"
            3 -> "Etage drei!"
            4 -> "Etage vier!"
            5 -> "Etage fünf!"
            6 -> "Etage sechs!"
            7 -> "Etage sieben!"
            8 -> "Etage acht!"
            9 -> "Etage neun!"
            10 -> "Etage zehn!"
            else -> "Etage $floor!"
        }
        voiceIntercom.speak(numberWord)
    }

    fun speakToddlerFloorAnnouncement(floor: Int) {
        val announcement = when (floor) {
            1 -> "Etage eins! Hier sind die bunten Bälle und Kuscheltiere."
            2 -> "Etage zwei! Schau mal, bunte Luftballons und Musik!"
            3 -> "Etage drei! Leckeres Eis und süße Waffeln!"
            4 -> "Etage vier! Tschu-Tschu, die kleine Spielzeug-Eisenbahn fährt!"
            5 -> "Etage fünf! Bunte Fingerfarben und Riesen-Buntstifte!"
            6 -> "Etage sechs! Ein großes Aquarium mit bunten Fischen!"
            7 -> "Etage sieben! Das Wolken-Hüpfkissen und Rutschen!"
            8 -> "Etage acht! Kleine Flugzeuge und Zaubersterne!"
            9 -> "Etage neun! Riesige Seifenblasen schweben durch die Luft!"
            10 -> "Etage zehn! Ganz oben bei den Wolken und der Sonne!"
            else -> "Wir sind auf Etage $floor angekommen!"
        }
        voiceIntercom.speak(announcement)
    }

    fun updateToddlerPin(newPin: String) {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            _uiState.update { it.copy(toddlerPin = newPin, toastMessage = "Eltern-PIN aktualisiert.") }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceIntercom.release()
        soundEngine.release()
    }
}
