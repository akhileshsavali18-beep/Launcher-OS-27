package com.example.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.QueryProductDetailsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubscriptionManager(context: Context) : ProductDetailsResponseListener,
  PurchasesUpdatedListener {

  companion object {
    const val MONTHLY_PRODUCT_ID = "launcher_premium_monthly"
    const val YEARLY_PRODUCT_ID = "launcher_premium_yearly"
    // Keep false until the Play Console app + subscriptions are created.
    // Switch to true before production release.
    const val PLAY_BILLING_ENABLED = false
  }

  private val prefs = context.applicationContext.getSharedPreferences("launcher_os_27", Context.MODE_PRIVATE)
  private val _premium = MutableStateFlow(if (!PLAY_BILLING_ENABLED) prefs.getBoolean("test_premium", false) else false)
  val premium: StateFlow<Boolean> = _premium.asStateFlow()
  private val _monthly = MutableStateFlow<ProductDetails?>(null)
  val monthly: StateFlow<ProductDetails?> = _monthly.asStateFlow()
  private val _yearly = MutableStateFlow<ProductDetails?>(null)
  val yearly: StateFlow<ProductDetails?> = _yearly.asStateFlow()

  private val billingClient = BillingClient.newBuilder(context.applicationContext)
    .setListener(this)
    .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
    .enableAutoServiceReconnection()
    .build()

  init { if (PLAY_BILLING_ENABLED) connect() }

  fun activateTestPremium() {
    if (!PLAY_BILLING_ENABLED) {
      prefs.edit().putBoolean("test_premium", true).apply()
      _premium.value = true
    }
  }

  fun deactivateTestPremium() {
    if (!PLAY_BILLING_ENABLED) {
      prefs.edit().putBoolean("test_premium", false).apply()
      _premium.value = false
    }
  }

  private fun connect() {
    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(result: com.android.billingclient.api.BillingResult) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
          queryProducts()
          restorePurchases()
        }
      }
      override fun onBillingServiceDisconnected() = Unit
    })
  }

  private fun queryProducts() {
    val products = listOf(MONTHLY_PRODUCT_ID, YEARLY_PRODUCT_ID).map {
      QueryProductDetailsParams.Product.newBuilder()
        .setProductId(it)
        .setProductType(BillingClient.ProductType.SUBS)
        .build()
    }
    billingClient.queryProductDetailsAsync(
      QueryProductDetailsParams.newBuilder().setProductList(products).build(), this
    )
  }

  override fun onProductDetailsResponse(
    result: com.android.billingclient.api.BillingResult,
    productDetailsResult: QueryProductDetailsResult
  ) {
    if (result.responseCode != BillingClient.BillingResponseCode.OK) return
    val productDetailsList = productDetailsResult.productDetailsList
    _monthly.value = productDetailsList.firstOrNull { it.productId == MONTHLY_PRODUCT_ID }
    _yearly.value = productDetailsList.firstOrNull { it.productId == YEARLY_PRODUCT_ID }
  }

  fun launchPurchase(activity: Activity, productDetails: ProductDetails): Boolean {
    if (!PLAY_BILLING_ENABLED) return false
    val offer = productDetails.subscriptionOfferDetails?.firstOrNull() ?: return false
    val params = BillingFlowParams.ProductDetailsParams.newBuilder()
      .setProductDetails(productDetails)
      .setOfferToken(offer.offerToken)
      .build()
    val flowParams = BillingFlowParams.newBuilder()
      .setProductDetailsParamsList(listOf(params))
      .build()
    return billingClient.launchBillingFlow(activity, flowParams).responseCode == BillingClient.BillingResponseCode.OK
  }

  fun restorePurchases() {
    if (!PLAY_BILLING_ENABLED) return
    if (!billingClient.isReady) return
    billingClient.queryPurchasesAsync(
      QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
    ) { result, purchases ->
      if (result.responseCode == BillingClient.BillingResponseCode.OK) updatePremium(purchases)
    }
  }

  override fun onPurchasesUpdated(
    result: com.android.billingclient.api.BillingResult,
    purchases: MutableList<Purchase>?
  ) {
    if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
      purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
        .forEach {
          val params = com.android.billingclient.api.AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(it.purchaseToken).build()
          billingClient.acknowledgePurchase(params) { }
        }
      updatePremium(purchases)
    }
  }

  private fun updatePremium(purchases: List<Purchase>) {
    _premium.value = purchases.any {
      it.purchaseState == Purchase.PurchaseState.PURCHASED &&
        it.products.any { p -> p == MONTHLY_PRODUCT_ID || p == YEARLY_PRODUCT_ID }
    }
  }
}
