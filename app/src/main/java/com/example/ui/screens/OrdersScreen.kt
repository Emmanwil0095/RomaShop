package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.ui.theme.PineGreen
import com.example.ui.theme.SageGreen
import com.example.ui.viewmodel.MarketplaceViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
  viewModel: MarketplaceViewModel,
  modifier: Modifier = Modifier
) {
  val orders by viewModel.userOrders.collectAsState()

  if (orders.isEmpty()) {
    EmptyOrdersView(onExplore = { viewModel.navigateTo(Screen.EXPLORE) }, modifier = modifier)
    return
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("orders_list"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Your Purchase History",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${orders.size} orders",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    items(
      items = orders,
      key = { it.id }
    ) { order ->
      OrderCard(order = order)
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@Composable
private fun OrderCard(order: OrderEntity) {
  val formattedDate = remember(order.createdAt) {
    SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()).format(Date(order.createdAt))
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: Order # and Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = null,
            tint = PineGreen,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "#${order.orderNumber}",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.primaryContainer
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = PineGreen,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = order.status,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = PineGreen
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = formattedDate,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
      Spacer(modifier = Modifier.height(10.dp))

      // Delivery & Items Recap
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.LocalShipping,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Delivering to: ${order.shippingAddress}",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Payment Method with Stripe Indicator
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = SageGreen,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Paid with ${order.paymentMethod}",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (order.paymentIntentId.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Stripe ID: ${order.paymentIntentId}",
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${order.itemCount} items purchased",
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "$${String.format("%.2f", order.totalAmount)}",
          fontSize = 17.sp,
          fontWeight = FontWeight.ExtraBold,
          color = PineGreen
        )
      }
    }
  }
}

@Composable
private fun EmptyOrdersView(onExplore: () -> Unit, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(32.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(80.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.ReceiptLong,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(40.dp)
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "No Orders Placed Yet",
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "When you purchase pre-loved treasures through our Stripe checkout, your receipts and tracking will appear here.",
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    Button(
      onClick = onExplore,
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
      Text("Start Browsing Items")
    }
  }
}
