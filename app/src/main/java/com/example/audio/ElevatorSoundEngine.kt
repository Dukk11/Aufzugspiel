package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.ElevatorMusicTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-performance real-time audio synthesizer for elevator music, subtle Apple-style
 * arrival chimes, cabin magnetic hum, and fading-in spatial mall atrium ambience.
 * Uses native Android AudioTrack with PCM 16-bit synthesis for zero-dependency,
 * responsive audio playback.
 */
class ElevatorSoundEngine {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null
    private var cabinHumJob: Job? = null
    private var mallAmbienceJob: Job? = null

    @Volatile
    private var isPlayingMusic = false

    @Volatile
    private var isCabinHumActive = false

    @Volatile
    private var isMallAmbienceActive = false

    @Volatile
    private var mallAmbienceTargetGain = 0.0

    private var currentTrack: ElevatorMusicTrack = ElevatorMusicTrack.BOSSA_BREEZE

    var musicVolume: Float = 0.7f
    var sfxVolume: Float = 0.8f

    @Volatile
    var humVolume: Float = 0.75f

    @Volatile
    var chimeVolume: Float = 0.85f

    @Volatile
    var mallAmbienceVolume: Float = 0.70f

    // Sample rate for synthesizer
    private val sampleRate = 22050

    fun isMusicPlaying(): Boolean = isPlayingMusic
    fun getCurrentTrack(): ElevatorMusicTrack = currentTrack

    /**
     * Starts or switches background elevator music for the given track.
     */
    fun startElevatorMusic(track: ElevatorMusicTrack = currentTrack) {
        currentTrack = track
        musicJob?.cancel()
        isPlayingMusic = true

        musicJob = scope.launch {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val audioTrack = buildStreamingAudioTrack(minBufferSize, AudioAttributes.CONTENT_TYPE_MUSIC)
                ?: return@launch

            try {
                audioTrack.play()

                val (melody, bassSequence, noteDurationMs) = when (track) {
                    ElevatorMusicTrack.BOSSA_BREEZE -> Triple(
                        listOf(
                            261.63, 329.63, 392.00, 493.88, 523.25, 392.00, 329.63, 261.63,
                            220.00, 261.63, 329.63, 440.00, 392.00, 329.63, 293.66, 261.63,
                            293.66, 349.23, 440.00, 523.25, 440.00, 349.23, 293.66, 261.63,
                            196.00, 246.94, 293.66, 392.00, 493.88, 392.00, 293.66, 246.94,
                            523.25, 659.25, 587.33, 523.25, 493.88, 440.00, 392.00, 329.63,
                            349.23, 392.00, 440.00, 493.88, 523.25, 392.00, 261.63, 0.0
                        ),
                        listOf(130.81, 110.00, 146.83, 98.00),
                        210
                    )

                    ElevatorMusicTrack.LOFI_SKYMALL -> Triple(
                        listOf(
                            349.23, 440.00, 523.25, 659.25, 523.25, 440.00, 392.00, 349.23,
                            329.63, 392.00, 493.88, 587.33, 493.88, 392.00, 329.63, 293.66,
                            293.66, 349.23, 440.00, 523.25, 440.00, 349.23, 293.66, 261.63,
                            261.63, 329.63, 392.00, 493.88, 440.00, 392.00, 349.23, 293.66
                        ),
                        listOf(87.31, 82.41, 73.42, 65.41),
                        275
                    )

                    ElevatorMusicTrack.VAPOR_HORIZON -> Triple(
                        listOf(
                            440.00, 523.25, 659.25, 880.00, 659.25, 523.25, 440.00, 349.23,
                            440.00, 523.25, 698.46, 523.25, 440.00, 349.23, 261.63, 329.63,
                            392.00, 523.25, 392.00, 329.63, 196.00, 246.94, 293.66, 392.00,
                            440.00, 523.25, 659.25, 880.00, 783.99, 659.25, 587.33, 523.25
                        ),
                        listOf(110.00, 87.31, 130.81, 98.00),
                        155
                    )

                    ElevatorMusicTrack.RETRO_MUZAK -> Triple(
                        listOf(
                            349.23, 440.00, 523.25, 440.00, 392.00, 523.25, 587.33, 523.25,
                            440.00, 349.23, 293.66, 349.23, 392.00, 440.00, 349.23, 261.63,
                            349.23, 440.00, 523.25, 587.33, 659.25, 587.33, 523.25, 440.00,
                            392.00, 440.00, 392.00, 329.63, 349.23, 392.00, 349.23, 0.0
                        ),
                        listOf(174.61, 130.81, 146.83, 130.81),
                        185
                    )

                    ElevatorMusicTrack.ZEN_CLOUD -> Triple(
                        listOf(
                            261.63, 392.00, 523.25, 659.25, 783.99, 523.25, 392.00, 440.00,
                            659.25, 880.00, 659.25, 587.33, 392.00, 493.88, 587.33, 783.99,
                            261.63, 329.63, 392.00, 523.25, 659.25, 783.99, 523.25, 392.00
                        ),
                        listOf(65.41, 55.00, 49.00, 65.41),
                        420
                    )
                }

                var noteIndex = 0
                val samplesPerNote = (sampleRate * noteDurationMs) / 1000
                val buffer = ShortArray(samplesPerNote)

                while (isActive && isPlayingMusic) {
                    val currentFreq = melody[noteIndex % melody.size]
                    val bassFreq = bassSequence[(noteIndex / 8) % bassSequence.size]

                    for (i in 0 until samplesPerNote) {
                        val t = i.toDouble() / sampleRate
                        val progress = i.toDouble() / samplesPerNote

                        val combinedWave = when (track) {
                            ElevatorMusicTrack.BOSSA_BREEZE -> {
                                val envelope = (1.0 - progress).coerceIn(0.0, 1.0)
                                val melodyWave = if (currentFreq > 10.0) {
                                    sin(2.0 * Math.PI * currentFreq * t) * 0.45 +
                                        sin(4.0 * Math.PI * currentFreq * t) * 0.15
                                } else 0.0
                                val bassWave = sin(2.0 * Math.PI * bassFreq * t) * 0.25
                                (melodyWave + bassWave) * envelope
                            }

                            ElevatorMusicTrack.LOFI_SKYMALL -> {
                                val envelope = exp(-progress * 2.5)
                                val wobble = 2.0 * sin(12.0 * t)
                                val melodyWave = if (currentFreq > 10.0) {
                                    sin(2.0 * Math.PI * (currentFreq + wobble) * t) * 0.40 +
                                        sin(4.0 * Math.PI * (currentFreq + wobble) * t) * 0.12
                                } else 0.0
                                val bassWave = sin(2.0 * Math.PI * bassFreq * t) * 0.30
                                (melodyWave + bassWave) * envelope
                            }

                            ElevatorMusicTrack.VAPOR_HORIZON -> {
                                val envelope = if (progress < 0.1) progress / 0.1 else (1.0 - progress * 0.7)
                                val melodyWave = if (currentFreq > 10.0) {
                                    sin(2.0 * Math.PI * currentFreq * t) * 0.35 +
                                        sin(6.0 * Math.PI * currentFreq * t) * 0.15 +
                                        sin(10.0 * Math.PI * currentFreq * t) * 0.08
                                } else 0.0
                                val bassWave = (sin(2.0 * Math.PI * bassFreq * t) +
                                    0.3 * sin(4.0 * Math.PI * bassFreq * t)) * 0.30
                                (melodyWave + bassWave) * envelope
                            }

                            ElevatorMusicTrack.RETRO_MUZAK -> {
                                val envelope = if (progress > 0.85) (1.0 - progress) / 0.15 else 1.0
                                val vibrato = 1.5 * sin(38.0 * t)
                                val melodyWave = if (currentFreq > 10.0) {
                                    sin(2.0 * Math.PI * (currentFreq + vibrato) * t) * 0.30 +
                                        sin(4.0 * Math.PI * (currentFreq + vibrato) * t) * 0.20 +
                                        sin(6.0 * Math.PI * (currentFreq + vibrato) * t) * 0.12
                                } else 0.0
                                val bassWave = sin(2.0 * Math.PI * bassFreq * t) * 0.22
                                (melodyWave + bassWave) * envelope
                            }

                            ElevatorMusicTrack.ZEN_CLOUD -> {
                                val attack = (progress / 0.25).coerceAtMost(1.0)
                                val decay = exp(-progress * 1.5)
                                val envelope = attack * decay
                                val melodyWave = if (currentFreq > 10.0) {
                                    sin(2.0 * Math.PI * currentFreq * t) * 0.35 +
                                        sin(5.52 * Math.PI * currentFreq * t) * 0.12 +
                                        sin(10.8 * Math.PI * currentFreq * t) * 0.06
                                } else 0.0
                                val bassWave = sin(2.0 * Math.PI * bassFreq * t) * 0.28
                                (melodyWave + bassWave) * envelope
                            }
                        }

                        val sampleShort = ((combinedWave * musicVolume).coerceIn(-1.0, 1.0) * 26000).toInt().toShort()
                        buffer[i] = sampleShort
                    }

                    audioTrack.write(buffer, 0, buffer.size)
                    noteIndex++
                }
            } catch (e: Exception) {
                // Ignore interruption
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // Clean up
                }
            }
        }
    }

    /**
     * Changes the current track and starts playing if music is active.
     */
    fun setMusicTrack(track: ElevatorMusicTrack) {
        currentTrack = track
        if (isPlayingMusic) {
            startElevatorMusic(track)
        }
    }

    /**
     * Stops the elevator lounge music gracefully.
     */
    fun stopElevatorMusic() {
        isPlayingMusic = false
        musicJob?.cancel()
        musicJob = null
    }

    /**
     * Starts a subtle, warm Apple-style cabin magnetic-levitation & ventilation background hum
     * while the elevator is riding, and softly settles upon arrival.
     */
    fun startCabinBackgroundHum() {
        if (sfxVolume <= 0.01f) return
        cabinHumJob?.cancel()
        isCabinHumActive = true

        cabinHumJob = scope.launch {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val audioTrack = buildStreamingAudioTrack(minBufferSize, AudioAttributes.CONTENT_TYPE_SONIFICATION)
                ?: return@launch

            try {
                audioTrack.play()
                val chunkSamples = sampleRate / 10 // 100ms chunks
                val buffer = ShortArray(chunkSamples)
                var phaseSample = 0L
                var currentEnvelope = 0.02

                while (isActive && isCabinHumActive) {
                    // Smooth fade-in to warm subtle hum level
                    currentEnvelope = (currentEnvelope + 0.08).coerceAtMost(1.0)
                    var lowPassNoise = 0.0

                    for (i in 0 until chunkSamples) {
                        val t = (phaseSample + i).toDouble() / sampleRate
                        // Warm 64 Hz magnetic drive fundamental + 128 Hz harmonic + slow LFO breath
                        val lfo = 0.85 + 0.15 * sin(2.0 * Math.PI * 0.65 * t)
                        val humFundamental = sin(2.0 * Math.PI * 64.0 * t) * 0.11
                        val humOvertone = sin(2.0 * Math.PI * 128.0 * t) * 0.055
                        val shimmerDrone = sin(2.0 * Math.PI * 192.0 * t) * 0.025

                        // Soft low-pass filtered air ventilation murmur
                        val white = (Math.random() - 0.5) * 0.04
                        lowPassNoise = lowPassNoise * 0.93 + white * 0.07

                        val wave = (humFundamental + humOvertone + shimmerDrone + lowPassNoise) *
                            lfo * currentEnvelope * humVolume * sfxVolume
                        buffer[i] = (wave.coerceIn(-1.0, 1.0) * 18000).toInt().toShort()
                    }
                    phaseSample += chunkSamples
                    audioTrack.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                // Ignore interruption
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // Clean up
                }
            }
        }
    }

    /**
     * Stops the continuous cabin ride hum and plays a warm settling arrival hum + subtle Apple chime.
     */
    fun triggerArrivalChimeAndHum(goingUp: Boolean = true) {
        isCabinHumActive = false
        cabinHumJob?.cancel()
        cabinHumJob = null

        if (sfxVolume <= 0.01f || (humVolume <= 0.01f && chimeVolume <= 0.01f)) return

        scope.launch {
            // 1.35-second composite buffer: warm magnetic leveling hum settling down + glassy Apple two-tone chime
            val totalDurationMs = 1350
            val samples = (sampleRate * totalDurationMs) / 1000
            val buffer = ShortArray(samples)

            // Ascending interval (E5 -> B5) for UP arrival, warm descending (B5 -> E5) for DOWN arrival
            val firstFreq = if (goingUp) 659.25 else 987.77
            val secondFreq = if (goingUp) 987.77 else 659.25
            val secondNoteStart = (sampleRate * 340) / 1000

            var filteredAir = 0.0
            val humGain = humVolume.toDouble()
            val chimeGain = chimeVolume.toDouble()

            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val overallProgress = i.toDouble() / samples

                // 1. Warm arrival leveling hum (64 Hz + 128 Hz + soft air cushion) that swells slightly and fades smoothly
                val humAttack = (overallProgress / 0.12).coerceAtMost(1.0)
                val humDecay = exp(-overallProgress * 2.6)
                val humEnv = humAttack * humDecay
                val rawAir = (Math.random() - 0.5) * 0.05
                filteredAir = filteredAir * 0.94 + rawAir * 0.06
                val arrivalHum = (
                    sin(2.0 * Math.PI * 64.0 * t) * 0.16 +
                        sin(2.0 * Math.PI * 128.0 * t) * 0.08 +
                        sin(2.0 * Math.PI * 192.0 * t) * 0.03 +
                        filteredAir
                    ) * humEnv * humGain

                // 2. Subtle Apple-style crystalline chime (Note 1 with lingering tail + Note 2 overlapping)
                val note1Attack = (i.toDouble() / (sampleRate * 0.012)).coerceAtMost(1.0)
                val note1Decay = exp(-t * 3.4)
                val note1Wave = (
                    sin(2.0 * Math.PI * firstFreq * t) * 0.38 +
                        sin(4.0 * Math.PI * firstFreq * t) * 0.11 +
                        sin(6.0 * Math.PI * firstFreq * t) * 0.04
                    ) * note1Attack * note1Decay * chimeGain

                val note2Wave = if (i >= secondNoteStart) {
                    val t2 = (i - secondNoteStart).toDouble() / sampleRate
                    val note2Attack = ((i - secondNoteStart).toDouble() / (sampleRate * 0.012)).coerceAtMost(1.0)
                    val note2Decay = exp(-t2 * 2.8)
                    (
                        sin(2.0 * Math.PI * secondFreq * t2) * 0.42 +
                            sin(4.0 * Math.PI * secondFreq * t2) * 0.13 +
                            sin(8.0 * Math.PI * secondFreq * t2) * 0.03
                        ) * note2Attack * note2Decay * chimeGain
                } else {
                    0.0
                }

                val combined = (arrivalHum + note1Wave + note2Wave) * sfxVolume
                buffer[i] = (combined.coerceIn(-1.0, 1.0) * 26000).toInt().toShort()
            }

            playOneShot(buffer)
        }
    }

    /**
     * Plays the signature elevator bell chime ("Ding-Dong!") when already on a floor or interacting.
     */
    fun playElevatorChime() {
        triggerArrivalChimeAndHum(goingUp = true)
    }

    /**
     * Fades in subtle, Apple-style spatial ambient mall sounds when the elevator doors open.
     * Combines a warm atrium fountain/breeze murmur, subtle cabin stationary hum, and
     * sparse crystalline atrium chime droplets tailored to the floor zone.
     */
    fun fadeInMallAmbience(floorNumber: Int) {
        if (sfxVolume <= 0.01f) return
        mallAmbienceTargetGain = 1.0
        if (isMallAmbienceActive && mallAmbienceJob?.isActive == true) {
            return
        }

        mallAmbienceJob?.cancel()
        isMallAmbienceActive = true

        mallAmbienceJob = scope.launch {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val audioTrack = buildStreamingAudioTrack(minBufferSize, AudioAttributes.CONTENT_TYPE_SONIFICATION)
                ?: return@launch

            try {
                audioTrack.play()
                val chunkSamples = sampleRate / 10 // 100ms chunks for responsive fade-in/out
                val buffer = ShortArray(chunkSamples)
                var currentGain = 0.0
                var sampleCounter = 0L
                var lowPassAtrium = 0.0
                var bandPassMurmur = 0.0

                // Zone-specific pentatonic shimmer frequencies for subtle spatial mall life
                val shimmerNotes = when {
                    floorNumber <= 15 -> listOf(523.25, 659.25, 783.99, 1046.50) // Candy / Warm C major pentatonic
                    floorNumber <= 45 -> listOf(587.33, 698.46, 880.00, 1174.66) // Arcade / Bright D dorian shimmer
                    floorNumber <= 75 -> listOf(392.00, 493.88, 587.33, 783.99)  // Safari & Skater / Open G acoustic
                    else -> listOf(440.00, 554.37, 659.25, 880.00, 1108.73)      // Sky Deck / Ethereal A major 9th
                }

                var chunkIndex = 0
                while (isActive && (isMallAmbienceActive || currentGain > 0.01)) {
                    // Smooth ~1.8 second Apple-style exponential fade-in when doors open, or smooth fade-out when closing
                    currentGain = if (isMallAmbienceActive && mallAmbienceTargetGain > 0.5) {
                        (currentGain + 0.055).coerceAtMost(1.0)
                    } else {
                        (currentGain - 0.14).coerceAtLeast(0.0)
                    }

                    if (!isMallAmbienceActive && currentGain <= 0.01) {
                        break
                    }

                    val activeShimmerFreq = shimmerNotes[(chunkIndex / 14) % shimmerNotes.size]
                    val shimmerPhaseInCycle = (chunkIndex % 14) / 14.0
                    val shimmerEnvelope = if (shimmerPhaseInCycle < 0.08) {
                        shimmerPhaseInCycle / 0.08
                    } else {
                        exp(-(shimmerPhaseInCycle - 0.08) * 3.8)
                    }

                    for (i in 0 until chunkSamples) {
                        val t = (sampleCounter + i).toDouble() / sampleRate

                        // 1. Subtle cabin & escalator background hum (warm 55 Hz + 110 Hz)
                        val backgroundHum = sin(2.0 * Math.PI * 55.0 * t) * 0.035 +
                            sin(2.0 * Math.PI * 110.0 * t) * 0.018

                        // 2. Soft atrium fountain & lively spatial air murmur (dual-stage low-pass filtered noise)
                        val rawNoise = (Math.random() - 0.5) * 0.12
                        lowPassAtrium = lowPassAtrium * 0.96 + rawNoise * 0.04
                        bandPassMurmur = bandPassMurmur * 0.88 + (lowPassAtrium) * 0.12
                        val slowSpatialWave = 0.75 + 0.25 * sin(2.0 * Math.PI * 0.28 * t)

                        // 3. Delicate Apple-style glass atrium chime droplet
                        val shimmerDroplet = (
                            sin(2.0 * Math.PI * activeShimmerFreq * t) * 0.032 +
                                sin(4.0 * Math.PI * activeShimmerFreq * t) * 0.010
                            ) * shimmerEnvelope

                        val ambientSample = (
                            backgroundHum +
                                (bandPassMurmur * slowSpatialWave) +
                                shimmerDroplet
                            ) * currentGain * mallAmbienceVolume * sfxVolume

                        buffer[i] = (ambientSample.coerceIn(-1.0, 1.0) * 18500).toInt().toShort()
                    }

                    sampleCounter += chunkSamples
                    chunkIndex++
                    audioTrack.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                // Ignore interruption
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // Clean up
                }
            }
        }
    }

    /**
     * Smoothly fades out the ambient mall sounds when the elevator doors close.
     */
    fun fadeOutMallAmbience() {
        mallAmbienceTargetGain = 0.0
        isMallAmbienceActive = false
    }

    /**
     * Plays a short live preview of the warm cabin magnetic-levitation hum at the current humVolume.
     */
    fun playHumPreview() {
        if (sfxVolume <= 0.01f || humVolume <= 0.01f) return
        scope.launch {
            val durationMs = 620
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            var lowPassAir = 0.0
            val gain = humVolume.toDouble()

            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / samples
                val env = sin(progress * Math.PI)
                val raw = (Math.random() - 0.5) * 0.05
                lowPassAir = lowPassAir * 0.93 + raw * 0.07
                val wave = (
                    sin(2.0 * Math.PI * 64.0 * t) * 0.24 +
                        sin(2.0 * Math.PI * 128.0 * t) * 0.12 +
                        sin(2.0 * Math.PI * 192.0 * t) * 0.05 +
                        lowPassAir
                    ) * env * gain * sfxVolume
                buffer[i] = (wave.coerceIn(-1.0, 1.0) * 26000).toInt().toShort()
            }
            playOneShot(buffer)
        }
    }

    /**
     * Plays a live preview of the crystalline Apple-style arrival chime at the current chimeVolume.
     */
    fun playChimePreview() {
        if (sfxVolume <= 0.01f || chimeVolume <= 0.01f) return
        scope.launch {
            val durationMs = 720
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            val secondStart = (sampleRate * 240) / 1000
            val gain = chimeVolume.toDouble()

            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val n1 = (sin(2.0 * Math.PI * 659.25 * t) * 0.42 + sin(4.0 * Math.PI * 659.25 * t) * 0.12) * exp(-t * 3.6)
                val n2 = if (i >= secondStart) {
                    val t2 = (i - secondStart).toDouble() / sampleRate
                    (sin(2.0 * Math.PI * 987.77 * t2) * 0.46 + sin(4.0 * Math.PI * 987.77 * t2) * 0.14) * exp(-t2 * 3.0)
                } else 0.0
                val wave = (n1 + n2) * gain * sfxVolume
                buffer[i] = (wave.coerceIn(-1.0, 1.0) * 27000).toInt().toShort()
            }
            playOneShot(buffer)
        }
    }

    /**
     * Plays a short spatial atrium shimmer & fountain preview at the current mallAmbienceVolume.
     */
    fun playMallAmbiencePreview(floorNumber: Int = 1) {
        if (sfxVolume <= 0.01f || mallAmbienceVolume <= 0.01f) return
        scope.launch {
            val durationMs = 750
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            var lowPassAtrium = 0.0
            val gain = mallAmbienceVolume.toDouble()
            val baseFreq = if (floorNumber <= 45) 587.33 else 523.25

            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / samples
                val env = sin(progress * Math.PI)
                val rawNoise = (Math.random() - 0.5) * 0.16
                lowPassAtrium = lowPassAtrium * 0.95 + rawNoise * 0.05
                val droplet1 = sin(2.0 * Math.PI * baseFreq * t) * 0.16 * exp(-t * 3.2)
                val droplet2 = if (progress > 0.35) {
                    val t2 = t - 0.26
                    sin(2.0 * Math.PI * (baseFreq * 1.5) * t2) * 0.18 * exp(-t2 * 3.2)
                } else 0.0
                val wave = (lowPassAtrium * env + droplet1 + droplet2) * gain * sfxVolume
                buffer[i] = (wave.coerceIn(-1.0, 1.0) * 26000).toInt().toShort()
            }
            playOneShot(buffer)
        }
    }

    /**
     * Crisp mechanical click / digital beep for elevator control buttons and dial ticks.
     */
    fun playButtonClick(freqHz: Double = 880.0) {
        if (sfxVolume <= 0.01f) return
        scope.launch {
            val durationMs = 50
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)

            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / samples
                val envelope = 1.0 - progress
                val wave = sin(2.0 * Math.PI * freqHz * t) * envelope * sfxVolume
                buffer[i] = (wave.coerceIn(-1.0, 1.0) * 25000).toInt().toShort()
            }

            playOneShot(buffer)
        }
    }

    /**
     * Sound when elevator doors slide open or close with pneumatic hiss and mechanical latch.
     */
    fun playDoorSlide() {
        if (sfxVolume <= 0.01f) return
        scope.launch {
            val durationMs = 450
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)

            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / samples
                val freq = 180.0 + progress * 140.0
                val envelope = sin(progress * Math.PI)
                val airHiss = (Math.random() - 0.5) * 0.35 * (1.0 - progress * 0.5)
                val motorTone = sin(2.0 * Math.PI * freq * t) * 0.35

                val clickTone = if (progress > 0.88) {
                    val clickP = (progress - 0.88) / 0.12
                    sin(2.0 * Math.PI * 950.0 * t) * (1.0 - clickP) * 0.4
                } else 0.0

                val wave = (motorTone + airHiss + clickTone) * envelope * sfxVolume
                buffer[i] = (wave.coerceIn(-1.0, 1.0) * 20000).toInt().toShort()
            }

            playOneShot(buffer)
        }
    }

    /**
     * Classic elevator emergency bell (Notruf-Glocke) ringing sound.
     */
    fun playAlarmBell() {
        if (sfxVolume <= 0.01f) return
        scope.launch {
            val durationMs = 600
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)

            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val tremolo = 0.6 + 0.4 * sin(2.0 * Math.PI * 18.0 * t)
                val decay = exp(-t * 2.8)
                val bellWave = (
                    sin(2.0 * Math.PI * 1568.0 * t) * 0.5 +
                        sin(2.0 * Math.PI * 1864.0 * t) * 0.3 +
                        sin(2.0 * Math.PI * 3136.0 * t) * 0.15
                    ) * tremolo * decay * sfxVolume

                buffer[i] = (bellWave.coerceIn(-1.0, 1.0) * 26000).toInt().toShort()
            }

            playOneShot(buffer)
        }
    }

    /**
     * Mechanical relay / switch latch sound for tactile buttons.
     */
    fun playMechanicalLatch() {
        if (sfxVolume <= 0.01f) return
        scope.launch {
            val durationMs = 60
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)

            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / samples
                val envelope = exp(-progress * 15.0)
                val clickWave = (sin(2.0 * Math.PI * 520.0 * t) * 0.6 + (Math.random() - 0.5) * 0.4) * envelope * sfxVolume
                buffer[i] = (clickWave.coerceIn(-1.0, 1.0) * 24000).toInt().toShort()
            }

            playOneShot(buffer)
        }
    }

    /**
     * Celebratory "Ka-Ching" cash register chime when buying an item or winning coins.
     */
    fun playKaChing() {
        if (sfxVolume <= 0.01f) return
        scope.launch {
            val durationMs = 600
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)

            val notes = listOf(1046.5, 1318.5, 1567.9, 2093.0)
            val segmentSamples = samples / notes.size

            for (i in 0 until samples) {
                val noteIdx = (i / segmentSamples).coerceAtMost(notes.size - 1)
                val freq = notes[noteIdx]
                val t = i.toDouble() / sampleRate
                val localT = (i % segmentSamples).toDouble() / segmentSamples
                val envelope = exp(-localT * 3.5)

                val wave = (sin(2.0 * Math.PI * freq * t) * 0.7 + sin(4.0 * Math.PI * freq * t) * 0.2) * envelope * sfxVolume
                buffer[i] = (wave.coerceIn(-1.0, 1.0) * 28000).toInt().toShort()
            }

            playOneShot(buffer)
        }
    }

    /**
     * Fun interaction sound when eating or using an item (munch, boing, blaster).
     */
    fun playItemAction(type: String) {
        if (sfxVolume <= 0.01f) return
        scope.launch {
            val durationMs = 350
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)

            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / samples
                val wave = when (type) {
                    "eat" -> {
                        val env = (1.0 - progress)
                        (sin(2.0 * Math.PI * (300.0 - progress * 150.0) * t) * 0.3 + (Math.random() - 0.5) * 0.5) * env
                    }
                    "toy" -> {
                        val env = exp(-progress * 3.0)
                        sin(2.0 * Math.PI * (440.0 + sin(progress * 25.0) * 120.0) * t) * env
                    }
                    "magic" -> {
                        val env = (1.0 - progress)
                        sin(2.0 * Math.PI * (1200.0 + progress * 800.0) * t) * 0.5 * env
                    }
                    else -> {
                        val env = (1.0 - progress)
                        sin(2.0 * Math.PI * 600.0 * t) * env
                    }
                } * sfxVolume

                buffer[i] = (wave.coerceIn(-1.0, 1.0) * 24000).toInt().toShort()
            }

            playOneShot(buffer)
        }
    }

    /**
     * Gentle, cheerful, bubble-like pop sound designed specifically for toddlers.
     * Soft harmonic sine sweep that sounds cozy and friendly rather than harsh.
     */
    fun playToddlerPop(freqHz: Double = 523.25) {
        if (sfxVolume <= 0.01f) return
        scope.launch {
            val durationMs = 120
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)

            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / samples
                // Gentle upward bubble sweep then smooth natural fade out
                val currentFreq = freqHz * (1.0 + sin(progress * Math.PI * 0.5) * 0.35)
                val envelope = sin(progress * Math.PI) * exp(-progress * 2.2)
                val wave = (
                    sin(2.0 * Math.PI * currentFreq * t) * 0.75 +
                        sin(4.0 * Math.PI * currentFreq * t) * 0.20
                    ) * envelope * sfxVolume
                buffer[i] = (wave.coerceIn(-1.0, 1.0) * 22000).toInt().toShort()
            }

            playOneShot(buffer)
        }
    }

    /**
     * Warm, calming marimba/harp chime note for toddler mode floor arrival.
     */
    fun playToddlerChime(noteIndex: Int = 0) {
        if (sfxVolume <= 0.01f) return
        scope.launch {
            val durationMs = 650
            val samples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(samples)
            val notes = listOf(523.25, 587.33, 659.25, 783.99, 880.00, 1046.50)
            val baseFreq = notes[noteIndex.coerceIn(0, notes.size - 1)]

            for (i in 0 until samples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / samples
                val envelope = exp(-progress * 3.8)
                val wave = (
                    sin(2.0 * Math.PI * baseFreq * t) * 0.65 +
                        sin(4.0 * Math.PI * baseFreq * t) * 0.22 +
                        sin(6.0 * Math.PI * baseFreq * t) * 0.08
                    ) * envelope * sfxVolume
                buffer[i] = (wave.coerceIn(-1.0, 1.0) * 24000).toInt().toShort()
            }

            playOneShot(buffer)
        }
    }

    private fun buildStreamingAudioTrack(minBufferSize: Int, contentType: Int): AudioTrack? {
        return try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(contentType)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            null
        }
    }

    private fun playOneShot(buffer: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            scope.launch {
                delay((buffer.size.toLong() * 1000L / sampleRate) + 100)
                try {
                    track.stop()
                    track.release()
                } catch (e: Exception) {
                    // ignore
                }
            }
        } catch (e: Exception) {
            // AudioTrack creation failure fallback
        }
    }

    fun release() {
        stopElevatorMusic()
        isCabinHumActive = false
        cabinHumJob?.cancel()
        fadeOutMallAmbience()
        mallAmbienceJob?.cancel()
    }
}
