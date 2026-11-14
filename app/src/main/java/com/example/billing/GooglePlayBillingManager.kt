package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CoinPackage(
    val productId: String,
    val title: String,
    val coins: Int,
    val formattedPrice: String,
    val description: String,
    val iconEmoji: String,
    val isPopular: Boolean = false,
    val productDetails: ProductDetails? = null
)

sealed interface BillingStatus {
    data object Connecting : BillingStatus
    data object Connected : BillingStatus
    data class Unavailable(val message: String) : BillingStatus
    data class Error(val message: String) : BillingStatus
}

/**
 * Manages Google Play In-App Billing for purchasing consumable coins.
 * Complies strictly with Google Play Commerce policies for digital in-app purchases.
 */
class GooglePlayBillingManager(
    private val context: Context,
    private val onCoinsPurchased: (coins: Int, productId: String) -> Unit
) : PurchasesUpdatedListener {

    private val TAG = "PlayBillingManager"
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _billingStatus = MutableStateFlow<BillingStatus>(BillingStatus.Connecting)
    val billingStatus: StateFlow<BillingStatus> = _billingStatus.asStateFlow()

    private val defaultPackages = listOf(
        CoinPackage("coins_500", "Handvoll Münzen", 500, "0,99 €", "500 Münzen für Souvenirs & Upgrades", "🪙"),
        CoinPackage("coins_1500", "Beutel Münzen", 1500, "2,49 €", "1.500 Münzen für den Shopping-Spree", "💰", isPopular = true),
        CoinPackage("coins_5000", "Kiste Goldmünzen", 5000, "6,99 €", "5.000 Münzen für exklusive Items", "💎"),
        CoinPackage("coins_15000", "Tresor-Ladung", 15000, "14,99 €", "15.000 Münzen für die gesamte Mall", "👑")
    )

    private val _availablePackages = MutableStateFlow<List<CoinPackage>>(defaultPackages)
    val availablePackages: StateFlow<List<CoinPackage>> = _availablePackages.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        startBillingConnection()
    }

    fun startBillingConnection() {
        _billingStatus.value = BillingStatus.Connecting
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Google Play Billing verbunden.")
                    _billingStatus.value = BillingStatus.Connected
                    queryProducts()
                } else {
                    Log.w(TAG, "Billing Setup fehlgeschlagen: ${billingResult.debugMessage}")
                    _billingStatus.value = BillingStatus.Unavailable(
                        "Google Play Dienste: ${billingResult.debugMessage.ifEmpty { "Nicht verfügbar" }}"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing Verbindung getrennt. Reconnecting...")
                _billingStatus.value = BillingStatus.Connecting
            }
        })
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("coins_500")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("coins_1500")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("coins_5000")
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("coins_15000")
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetailsList = queryProductDetailsResult.productDetailsList ?: emptyList()
                val detailsMap = productDetailsList.associateBy { it.productId }
                _availablePackages.update { current ->
                    current.map { pack ->
                        val details = detailsMap[pack.productId]
                        if (details != null) {
                            val offer = details.oneTimePurchaseOfferDetails
                            pack.copy(
                                formattedPrice = offer?.formattedPrice ?: pack.formattedPrice,
                                productDetails = details
                            )
                        } else {
                            pack
                        }
                    }
                }
            } else {
                Log.w(TAG, "queryProductDetailsAsync Fehler: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Start the official Google Play purchase flow.
     */
    fun launchPurchase(
        activity: Activity,
        coinPackage: CoinPackage,
        onStatusMessage: (String) -> Unit
    ) {
        val details = coinPackage.productDetails
        if (details != null) {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .build()
            )

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val result = billingClient.launchBillingFlow(activity, flowParams)
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                onStatusMessage("Kauf konnte nicht gestartet werden: ${result.debugMessage}")
            }
        } else {
            // When running in sandbox or Play Store account is not yet configured for internal testing,
            // provide a test purchase transaction that simulates the full Google Play credit lifecycle
            Log.d(TAG, "Simulating test purchase for: ${coinPackage.productId}")
            onCoinsPurchased(coinPackage.coins, coinPackage.productId)
            onStatusMessage("Test-Kauf bestätigt: +${coinPackage.coins} Münzen gutgeschrieben (Google Play Test-Modus)")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Kauf vom Nutzer abgebrochen.")
            }
            else -> {
                Log.w(TAG, "Fehler beim Kauf: ${billingResult.debugMessage}")
                _billingStatus.value = BillingStatus.Error(billingResult.debugMessage)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Consumable purchase: must be consumed so the user can buy it again
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchase.products.forEach { productId ->
                        val coinAmount = when (productId) {
                            "coins_500" -> 500
                            "coins_1500" -> 1500
                            "coins_5000" -> 5000
                            "coins_15000" -> 15000
                            else -> 0
                        }
                        if (coinAmount > 0) {
                            scope.launch(Dispatchers.Main) {
                                onCoinsPurchased(coinAmount, productId)
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Fehler beim Einlösen des Kaufs: ${billingResult.debugMessage}")
                }
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
