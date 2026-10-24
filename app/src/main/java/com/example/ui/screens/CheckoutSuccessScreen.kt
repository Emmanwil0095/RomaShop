package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PineGreen
import com.example.ui.theme.SageGreen
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.MarketplaceViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CheckoutSuccessScreen(
  viewModel: MarketplaceViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateTo(Screen.EXPLORE)
  }

  val lastOrder by viewModel.lastPlacedOrder.collectAsState()
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // 1. Success Circle Checkmark
    Box(
      modifier = Modifier
        .size(96.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(PineGreen),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(44.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Payment Authorized!",
      fontSize = 24.sp,
      fontWeight = FontWeight.ExtraBold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "Your secondhand purchase is confirmed and on its way.",
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // 2. Receipt Card
    if (lastOrder != null) {
      val order = lastOrder!!
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Order Number",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "#${order.orderNumber}",
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = "PAID (STRIPE)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PineGreen,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
          Spacer(modifier = Modifier.height(14.dp))

          SuccessInfoRow("Customer", order.buyerName)
          SuccessInfoRow("Recipient Email", order.buyerEmail)
          SuccessInfoRow("Shipping Address", order.shippingAddress)
          SuccessInfoRow("Payment Method", order.paymentMethod)
          if (order.paymentIntentId.isNotBlank()) {
            SuccessInfoRow("Stripe Intent ID", order.paymentIntentId.take(18) + "...")
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Total Charged",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "$${String.format("%.2f", order.totalAmount)}",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 20.sp,
              color = PineGreen
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Sustainability perk
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                tint = PineGreen,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Thank you for shopping circularly! You helped keep items in circulation.",
                fontSize = 11.sp,
                color = PineGreen,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // 3. Action Buttons
    Button(
      onClick = { viewModel.navigateTo(Screen.ORDERS) },
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("view_orders_button")
    ) {
      Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Track in Orders History", fontWeight = FontWeight.Bold, color = Color.White)
    }

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedButton(
      onClick = { viewModel.navigateTo(Screen.EXPLORE) },
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("continue_browsing_button")
    ) {
      Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Continue Browsing", fontWeight = FontWeight.SemiBold)
    }
  }
}

@Composable
private fun SuccessInfoRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(
      text = value,
      fontSize = 12.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1
    )
  }
}
