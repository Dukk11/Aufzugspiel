package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.billing.GooglePlayBillingManager
import com.example.ui.BackpackScreen
import com.example.ui.CoinShopDialog
import com.example.ui.ElevatorCabinView
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.ui.ElevatorAssistantDialog
import com.example.ui.ElevatorDialControl
import com.example.ui.ElevatorModelDialog
import com.example.ui.ElevatorMusicDialog
import com.example.ui.ElevatorMusicPlayerBar
import com.example.ui.ElevatorTopBar
import com.example.ui.FloorDirectoryDialog
import com.example.ui.FloorMatrixDialog
import com.example.ui.LegalDialog
import com.example.ui.MallFloorScreen
import com.example.ui.SettingsDialog
import com.example.ui.ShopDetailScreen
import com.example.ui.ToddlerCabinView
import com.example.ui.ToddlerParentExitDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ElevatorGameState
import com.example.viewmodel.ElevatorGameViewModel
import androidx.compose.runtime.rememberCoroutineScope
import androidx.credentials.CredentialManager
import com.example.ui.SignInScreen
import com.example.ui.authStateFlow
import com.example.ui.onGoogleSignInClicked
import com.example.ui.signOut
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay

private const val TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(TAG, "App Check debug secret configured successfully.")
            } else {
                Log.w(TAG, "No debug token found in Intent extra.")
            }

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
                } else {
                    Log.e(TAG, "recaptcha_site_key resource string is blank.")
                }
            } else {
                Log.e(TAG, "recaptcha_site_key string resource not found in package.")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            val viewModel: ElevatorGameViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            MyApplicationTheme(
                themeMode = state.themeMode,
                dynamicColor = state.useDynamicColor
            ) {
                ElevatorApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ElevatorApp(
    viewModel: ElevatorGameViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = context as? Activity
    val billingManager = remember {
        GooglePlayBillingManager(context) { coins, productId ->
            viewModel.creditPurchasedCoins(coins, productId)
        }
    }
    val billingPackages by billingManager.availablePackages.collectAsStateWithLifecycle()
    val billingStatus by billingManager.billingStatus.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.initVibrator(context)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Auto-clear toast feedback
    LaunchedEffect(state.toastMessage) {
        if (state.toastMessage != null) {
            delay(3200)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
            // Main Screen Content
            when (val screen = state.currentScreen) {
                is AppScreen.Elevator -> {
                    if (state.isToddlerMode) {
                        // 🧸 Calm, peaceful, distraction-free Toddler Cabin View
                        ToddlerCabinView(
                            state = state,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            onFloorSelected = { targetFloor ->
                                viewModel.playToddlerButtonTap(targetFloor)
                                viewModel.travelToFloor(targetFloor)
                            },
                            onOpenDoors = { viewModel.openDoorsManual() },
                            onCloseDoors = { viewModel.closeDoorsManual() },
                            onRequestParentExit = { viewModel.toggleToddlerExitDialog(true) },
                            onDirectExitSuccess = { viewModel.disableToddlerMode() }
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Compact Top Bar — hides while riding so only the elevator interior is visible
                            AnimatedVisibility(
                                visible = !state.isMoving,
                                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                            ) {
                                ElevatorTopBar(
                                    state = state,
                                    onOpenSettings = { viewModel.toggleSettingsDialog(true) },
                                    onOpenBackpack = { viewModel.openBackpack() },
                                    onOpenDirectory = { viewModel.toggleDirectoryDialog(true) },
                                    onOpenCoinShop = { viewModel.toggleCoinShopDialog(true) },
                                    onOpenModelSelect = { viewModel.toggleModelSelectDialog(true) },
                                    onOpenFloorMatrix = { viewModel.toggleFloorMatrixDialog(true) },
                                    onToggleMusic = { viewModel.toggleMusicPlayPause() },
                                    onOpenMusicCatalog = { viewModel.toggleMusicPlayerDialog(true) }
                                )
                            }

                            // Elevator Cabin View — expands to 100% fullscreen interior while riding
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = if (state.isMoving) 0.dp else 8.dp,
                                        vertical = if (state.isMoving) 0.dp else 2.dp
                                    )
                            ) {
                                ElevatorCabinView(
                                    state = state,
                                    modifier = Modifier.fillMaxSize(),
                                    onRingBell = { viewModel.ringEmergencyBell() },
                                    onFloorButtonClick = { viewModel.toggleFloorMatrixDialog(true) },
                                    onToggleMusic = { viewModel.toggleMusicPlayPause() },
                                    onSelectViewAngle = { viewModel.selectCabinViewAngle(it) },
                                    onCycleViewAngle = { viewModel.cycleCabinViewAngle(it) },
                                    onTravelToFloor = { viewModel.travelToFloor(it) },
                                    onSpeedModeChange = { viewModel.updateSpeedMode(it) },
                                    onStepIntoMall = { viewModel.stepIntoMall() },
                                    onOpenDoors = { viewModel.openDoorsManual() },
                                    onCloseDoors = { viewModel.closeDoorsManual() },
                                    onToggleAudioPanel = { viewModel.toggleCabinAudioPanel() },
                                    onHumVolumeChange = { vol, preview -> viewModel.updateHumVolume(vol, preview) },
                                    onChimeVolumeChange = { vol, preview -> viewModel.updateChimeVolume(vol, preview) },
                                    onMallAmbienceVolumeChange = { vol, preview -> viewModel.updateMallAmbienceVolume(vol, preview) },
                                    onApplyAudioPreset = { viewModel.applyCabinAudioPreset(it) },
                                    onPreviewAudioChannel = { viewModel.previewCabinAudioChannel(it) },
                                    onToggleCabinCustomizer = { viewModel.toggleCabinCustomizer() },
                                    onSelectWallMaterial = { viewModel.updateWallMaterial(it) },
                                    onSelectLightingColor = { viewModel.updateLightingColor(it) }
                                )
                            }

                            // Bottom Controls — hides while riding so passenger stands completely inside the cabin
                            AnimatedVisibility(
                                visible = !state.isMoving,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                            ) {
                                ElevatorDialControl(
                                    state = state,
                                    onFloorSelected = { viewModel.selectTargetFloor(it) },
                                    onAdjustFloor = { viewModel.adjustTargetFloorBy(it) },
                                    onStartRide = { viewModel.startElevatorRide() },
                                    onStepIntoMall = { viewModel.stepIntoMall() },
                                    onOpenDirectory = { viewModel.toggleDirectoryDialog(true) },
                                    onOpenDoors = { viewModel.openDoorsManual() },
                                    onCloseDoors = { viewModel.closeDoorsManual() },
                                    onRingBell = { viewModel.ringEmergencyBell() },
                                    onOpenFloorMatrix = { viewModel.toggleFloorMatrixDialog(true) },
                                    onSpeedModeChange = { viewModel.updateSpeedMode(it) }
                                )
                            }
                        }
                    }
                }

                is AppScreen.MallFloor -> {
                    MallFloorScreen(
                        state = state,
                        onEnterShop = { viewModel.enterShop(it) },
                        onReturnToElevator = { viewModel.returnToElevator() },
                        onClaimFountainCoins = { viewModel.claimFountainCoins() },
                        onOpenBackpack = { viewModel.openBackpack() },
                        onOpenCoinShop = { viewModel.toggleCoinShopDialog(true) },
                        onBuyItem = { viewModel.buyItem(it) },
                        onUseItem = { viewModel.useItem(it) },
                        onDismissItemEffect = { viewModel.dismissItemEffect() }
                    )
                }

                is AppScreen.ShopDetail -> {
                    ShopDetailScreen(
                        shop = screen.shop,
                        state = state,
                        onLeaveShop = { viewModel.leaveShop() },
                        onBuyItem = { viewModel.buyItem(it) },
                        onUseItem = { viewModel.useItem(it) },
                        onOpenBackpack = { viewModel.openBackpack() },
                        onOpenCoinShop = { viewModel.toggleCoinShopDialog(true) },
                        onDismissItemEffect = { viewModel.dismissItemEffect() }
                    )
                }

                is AppScreen.Backpack -> {
                    BackpackScreen(
                        state = state,
                        onClose = { viewModel.closeBackpack() },
                        onUseItem = { viewModel.useItem(it) }
                    )
                }
            }

            // In-Game Notification Toast at top (Material 3 Expressive Pill)
            AnimatedVisibility(
                visible = state.toastMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    ),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = state.toastMessage ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }

            // Settings Dialog ("Oben sollen Einstellungen stehen")
            if (state.showSettingsDialog) {
                SettingsDialog(
                    state = state,
                    onDismiss = { viewModel.toggleSettingsDialog(false) },
                    onMusicVolumeChange = { viewModel.updateMusicVolume(it) },
                    onSfxVolumeChange = { viewModel.updateSfxVolume(it) },
                    onSpeedModeChange = { viewModel.updateSpeedMode(it) },
                    onCabinThemeChange = { viewModel.updateCabinTheme(it) },
                    onAddPocketMoney = { viewModel.addPocketMoney(250) },
                    onOpenLegal = { viewModel.toggleLegalDialog(true) },
                    onOpenCoinShop = {
                        viewModel.toggleSettingsDialog(false)
                        viewModel.toggleCoinShopDialog(true)
                    },
                    onSelectTrack = { viewModel.selectMusicTrack(it) },
                    onOpenMusicCatalog = {
                        viewModel.toggleSettingsDialog(false)
                        viewModel.toggleMusicPlayerDialog(true)
                    },
                    onSelectModel = { viewModel.selectElevatorModel(it) },
                    onOpenModelDialog = {
                        viewModel.toggleSettingsDialog(false)
                        viewModel.toggleModelSelectDialog(true)
                    },
                    onHumVolumeChange = { viewModel.updateHumVolume(it, false) },
                    onChimeVolumeChange = { viewModel.updateChimeVolume(it, false) },
                    onMallAmbienceVolumeChange = { viewModel.updateMallAmbienceVolume(it, false) },
                    onWallMaterialChange = { viewModel.updateWallMaterial(it) },
                    onLightingColorChange = { viewModel.updateLightingColor(it) },
                    onToggleToddlerMode = {
                        if (state.isToddlerMode) {
                            viewModel.toggleSettingsDialog(false)
                            viewModel.toggleToddlerExitDialog(true)
                        } else {
                            viewModel.enableToddlerMode()
                        }
                    },
                    onThemeModeChange = { viewModel.updateThemeMode(it) },
                    onToggleDynamicColor = { viewModel.toggleDynamicColor(it) },
                    onToggleHaptics = { viewModel.toggleHaptics(it) },
                    onSignOut = {
                        val credentialManager = CredentialManager.create(context)
                        signOut(context, credentialManager, onSignOutComplete = {
                            viewModel.toggleSettingsDialog(false)
                        }, scope = scope)
                    },
                    onSignInWithGoogle = {
                        val credentialManager = CredentialManager.create(context)
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                viewModel.toggleSettingsDialog(false)
                            },
                            onAuthError = { /* Keep dialog */ },
                            scope = scope
                        )
                    }
                )
            }

            // Parental Exit Dialog (PIN & 3-Sekunden-Halte-Schutz)
            if (state.showToddlerExitDialog) {
                ToddlerParentExitDialog(
                    correctPin = state.toddlerPin,
                    onSuccess = {
                        viewModel.disableToddlerMode()
                    },
                    onDismiss = {
                        viewModel.toggleToddlerExitDialog(false)
                    }
                )
            }

            // Dedicated Elevator Model Selection Dialog
            if (state.showModelSelectDialog) {
                ElevatorModelDialog(
                    currentModel = state.currentModel,
                    onSelectModel = { viewModel.selectElevatorModel(it) },
                    onDismiss = { viewModel.toggleModelSelectDialog(false) }
                )
            }

            // Elevator Background Music Player & Jukebox Dialog
            if (state.showMusicPlayerDialog) {
                ElevatorMusicDialog(
                    currentTrack = state.currentMusicTrack,
                    isMusicPlaying = state.isMusicPlaying,
                    musicVolume = state.musicVolume,
                    onSelectTrack = { viewModel.selectMusicTrack(it) },
                    onTogglePlayPause = { viewModel.toggleMusicPlayPause() },
                    onVolumeChange = { viewModel.updateMusicVolume(it) },
                    onDismiss = { viewModel.toggleMusicPlayerDialog(false) }
                )
            }

            // Google Play In-App Coin Shop Dialog
            if (state.showCoinShopDialog) {
                CoinShopDialog(
                    currentCoins = state.coins,
                    packages = billingPackages,
                    billingStatus = billingStatus,
                    onBuyPackage = { pack ->
                        if (activity != null) {
                            billingManager.launchPurchase(activity, pack) { statusMsg ->
                                // Callback handles confirmation
                            }
                        }
                    },
                    onDismiss = { viewModel.toggleCoinShopDialog(false) }
                )
            }

            // Legal & Compliance Dialog (DSGVO, Impressum § 5 DDG, Jugendschutz, Lizenzen)
            if (state.showLegalDialog) {
                LegalDialog(
                    onDismiss = { viewModel.toggleLegalDialog(false) },
                    onResetData = { viewModel.resetAllGameData() }
                )
            }

            // 120-Floor Directory Dialog
            if (state.showDirectoryDialog) {
                FloorDirectoryDialog(
                    currentFloor = state.currentFloor,
                    targetFloor = state.targetFloor,
                    visitedFloors = state.visitedFloors,
                    isMoving = state.isMoving,
                    doorsOpen = state.doorsOpen,
                    onFloorSelected = { viewModel.selectTargetFloor(it) },
                    onTravelToFloor = { viewModel.travelToFloor(it) },
                    onStepIntoMall = { viewModel.stepIntoMall() },
                    onDismiss = { viewModel.toggleDirectoryDialog(false) }
                )
            }

            // Fullscreen 1–100 Elevator Floor Matrix Dialog
            if (state.showFloorMatrixDialog) {
                FloorMatrixDialog(
                    currentFloor = state.currentFloor,
                    targetFloor = state.targetFloor,
                    visitedFloors = state.visitedFloors,
                    isMoving = state.isMoving,
                    onSelectFloor = { viewModel.selectTargetFloor(it) },
                    onTravelToFloor = { viewModel.travelToFloor(it) },
                    onDismiss = { viewModel.toggleFloorMatrixDialog(false) }
                )
            }

            // SKY-AI Elevator Voice Intercom Dialog (Bell 🔔)
            if (state.showAssistantDialog) {
                ElevatorAssistantDialog(
                    state = state,
                    onSendVoiceCommand = { viewModel.sendAssistantMessage(it) },
                    onStartListening = { viewModel.startVoiceListening(context) },
                    onStopListening = { viewModel.stopVoiceListening() },
                    onReplaySpeech = { viewModel.replayIntercomSpeech() },
                    onToggleMute = { viewModel.toggleIntercomMute() },
                    onSimulateStuck = { viewModel.simulateEmergencyStuck() },
                    onEmergencyRescue = { viewModel.performEmergencyRescue() },
                    onTravelToFloor = { viewModel.travelToFloor(it) },
                    onToggleMusic = { viewModel.toggleMusicPlayPause() },
                    onOpenDoors = { viewModel.openDoorsManual() },
                    onDismiss = { viewModel.toggleAssistantDialog(false) }
                )
            }
            }
        }
    }
}
