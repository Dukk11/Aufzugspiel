package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

private const val TAG = "ElevatorVoiceIntercom"

/**
 * Real Voice Intercom Engine for SKY-AI:
 * - Speaks SKY-AI announcements aloud in clear German Bordcomputer voice via Android TextToSpeech,
 *   framed by realistic intercom chime tones.
 * - Listens to passenger voice commands via Android SpeechRecognizer.
 */
class ElevatorVoiceIntercom(
    private val soundEngine: ElevatorSoundEngine
) {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var pendingSpeechText: String? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var fallbackCadenceJob: Job? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _voiceMuted = MutableStateFlow(false)
    val voiceMuted: StateFlow<Boolean> = _voiceMuted.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    fun init(context: Context) {
        if (tts != null) return
        val appContext = context.applicationContext
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.GERMAN)
                    isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                            result != TextToSpeech.LANG_NOT_SUPPORTED
                    // Crisp Apple Bordcomputer voice tuning
                    tts?.setPitch(1.03f)
                    tts?.setSpeechRate(1.04f)
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            soundEngine.playButtonClick(660.0)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                    })

                    pendingSpeechText?.let { textToSpeak ->
                        pendingSpeechText = null
                        speak(textToSpeak)
                    }
                } else {
                    Log.w(TAG, "TTS initialization returned status $status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextToSpeech", e)
        }
    }

    /**
     * Speaks the given text out loud with an intercom chime prefix.
     * Strips emojis so the German TTS reads only natural spoken words.
     */
    fun speak(rawText: String) {
        if (_voiceMuted.value) return
        val cleanText = sanitizeForSpeech(rawText)
        if (cleanText.isBlank()) return

        fallbackCadenceJob?.cancel()

        // Play authentic intercom activation two-tone chime
        soundEngine.playButtonClick(880.0)
        scope.launch {
            delay(90)
            soundEngine.playButtonClick(1174.66) // D6 intercom Roger tone
        }

        val engine = tts
        if (engine != null && isTtsReady) {
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            val res = engine.speak(
                cleanText,
                TextToSpeech.QUEUE_FLUSH,
                params,
                "sky_ai_${System.currentTimeMillis()}"
            )
            if (res == TextToSpeech.ERROR) {
                runFallbackVoiceCadence(cleanText)
            }
        } else {
            pendingSpeechText = cleanText
            runFallbackVoiceCadence(cleanText)
        }
    }

    /**
     * If system TTS engine is still warming up or unavailable in a headless container,
     * animate the voice orb and play subtle sci-fi bordcomputer voice telemetry pulses.
     */
    private fun runFallbackVoiceCadence(cleanText: String) {
        fallbackCadenceJob?.cancel()
        fallbackCadenceJob = scope.launch {
            _isSpeaking.value = true
            val wordCount = cleanText.split(Regex("\\s+")).size.coerceIn(4, 28)
            repeat(wordCount) { idx ->
                if (idx % 3 == 0 && !_voiceMuted.value) {
                    val freq = 520.0 + ((idx * 47) % 240)
                    soundEngine.playButtonClick(freq)
                }
                delay(140L)
            }
            _isSpeaking.value = false
        }
    }

    fun stopSpeaking() {
        fallbackCadenceJob?.cancel()
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        _isSpeaking.value = false
    }

    fun toggleMute(): Boolean {
        val newMuted = !_voiceMuted.value
        _voiceMuted.value = newMuted
        if (newMuted) {
            stopSpeaking()
        }
        return newMuted
    }

    /**
     * Starts listening via the device microphone using Android SpeechRecognizer.
     */
    fun startListening(
        context: Context,
        onFinalResult: (String) -> Unit,
        onErrorMessage: (String) -> Unit
    ) {
        stopSpeaking()
        mainHandler.post {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    onErrorMessage("Spracherkennung auf diesem Gerät nicht verfügbar – nutze einfach die Intercom-Tasten darunter!")
                    return@post
                }

                speechRecognizer?.destroy()
                val recognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
                speechRecognizer = recognizer

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _liveTranscript.value = "Ich höre zu…"
                        soundEngine.playButtonClick(987.77)
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH,
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Ich habe dich akustisch nicht ganz verstanden. Drücke nochmal aufs Mikrofon oder wähle einen Direkt-Ruf!"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Bitte Mikrofon-Berechtigung für die Gegensprechanlage erlauben."
                            else -> "Funkstörung im Aufzugschacht – nutze einfach die Schnellruf-Tasten oder tippe deine Frage ein!"
                        }
                        _liveTranscript.value = ""
                        onErrorMessage(msg)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spoken = matches?.firstOrNull()?.trim().orEmpty()
                        _liveTranscript.value = spoken
                        if (spoken.isNotEmpty()) {
                            onFinalResult(spoken)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            ?.trim()
                            .orEmpty()
                        if (partial.isNotEmpty()) {
                            _liveTranscript.value = partial
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                recognizer.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "SpeechRecognizer failed to start", e)
                _isListening.value = false
                onErrorMessage("Mikrofon konnte nicht gestartet werden.")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {
            }
            _isListening.value = false
        }
    }

    fun release() {
        fallbackCadenceJob?.cancel()
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {
            }
        }
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {
        }
    }

    private fun sanitizeForSpeech(text: String): String {
        // Remove emojis and special bullet symbols so German TTS speaks smoothly
        return text
            .replace(Regex("[\\p{So}\\p{Cn}]"), "")
            .replace("•", ", ")
            .replace("Etage ", "Etage ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
