package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.billingclient.api.ProductDetails
import com.example.ads.AdsManager
import com.example.billing.SubscriptionManager
import com.example.ui.theme.GlassWhiteHigh
import com.example.ui.theme.GlassWhiteMedium
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun LauncherBannerAd(
  visible: Boolean,
  modifier: Modifier = Modifier
) {
  if (!visible) return
  AndroidView(
    modifier = modifier.fillMaxWidth().height(52.dp),
    factory = { context ->
      AdView(context).apply {
        adUnitId = AdsManager.BANNER_TEST_ID
        setAdSize(AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, 360))
        loadAd(AdRequest.Builder().build())
      }
    }
  )
}

@Composable
fun PremiumSheet(
  manager: SubscriptionManager,
  activity: Activity,
  onDismiss: () -> Unit
) {
  val premium by manager.premium.collectAsState()
  val monthly by manager.monthly.collectAsState()
  val yearly by manager.yearly.collectAsState()

  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = Color(0xFF111827),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White)
        Spacer(Modifier.size(8.dp))
        Text("Launcher OS 27+", color = Color.White)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Premium removes ads and unlocks the premium launcher experience.", color = Color.White.copy(alpha = .85f), fontSize = 14.sp)
        listOf("No ads", "Premium glass themes", "Advanced launcher customization", "Priority features").forEach {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color(0xFF67E8F9), modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(7.dp))
            Text(it, color = Color.White, fontSize = 13.sp)
          }
        }
        if (premium) {
          Text(
            if (SubscriptionManager.PLAY_BILLING_ENABLED) "Premium is active on this Google Play account."
            else "TEST PREMIUM is active (Play Console not connected yet).",
            color = Color(0xFF86EFAC), fontSize = 13.sp
          )
        } else if (!SubscriptionManager.PLAY_BILLING_ENABLED) {
          Text("Development mode: Google Play Billing is intentionally disabled until your Play Console account is ready.", color = Color.White.copy(alpha = .65f), fontSize = 12.sp)
          Button(onClick = { manager.activateTestPremium() }, modifier = Modifier.fillMaxWidth()) {
            Text("Activate Test Premium")
          }
        } else {
          PlanButton("Monthly", monthly) { monthly?.let { manager.launchPurchase(activity, it) } }
          PlanButton("Yearly", yearly) { yearly?.let { manager.launchPurchase(activity, it) } }
          Text("Products must be created in Play Console with IDs launcher_premium_monthly and launcher_premium_yearly.", color = Color.White.copy(alpha = .55f), fontSize = 11.sp)
        }
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { if (SubscriptionManager.PLAY_BILLING_ENABLED) manager.restorePurchases() else manager.deactivateTestPremium() }, colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = .12f))) {
          Text(if (SubscriptionManager.PLAY_BILLING_ENABLED) "Restore" else "Reset Test", color = Color.White)
        }
      }
    }
  )
}

@Composable
private fun PlanButton(label: String, product: ProductDetails?, onClick: () -> Unit) {
  val price = product?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
  Button(
    onClick = onClick,
    enabled = product != null,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = .14f))
  ) {
    Text(if (price != null) "$label  •  $price" else "$label  •  Loading…", color = Color.White)
  }
}
