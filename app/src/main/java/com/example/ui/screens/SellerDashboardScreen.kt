package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.ProductEntity
import com.example.ui.theme.PineGreen
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.MarketplaceViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun SellerDashboardScreen(
  viewModel: MarketplaceViewModel,
  modifier: Modifier = Modifier
) {
  val user by viewModel.currentUser.collectAsState()
  val activeListings by viewModel.sellerActiveListings.collectAsState()
  val soldListings by viewModel.sellerSoldListings.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }

  val totalSalesEst = (soldListings.sumOf { it.price } + (user.salesCount * 55.0))

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(6.dp))

        // 1. Seller Stats Card
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(PineGreen),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = user.name.take(2).uppercase(),
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 17.sp
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = user.name,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${user.email} • ${user.joinedDate}",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              StatColumn(value = activeListings.size.toString(), label = "Active Items")
              StatColumn(value = "${soldListings.size + user.salesCount}", label = "Items Sold")
              StatColumn(value = "$${String.format("%.0f", totalSalesEst)}", label = "Total Sales")
              StatColumn(value = "${String.format("%.1f", user.rating)}★", label = "Rating")
            }
          }
        }
      }

      // 2. Tabs
      item {
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.Transparent,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = PineGreen
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = {
              Text(
                text = "Active Listings (${activeListings.size})",
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 0) PineGreen else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = {
              Text(
                text = "Sold (${soldListings.size})",
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedTab == 1) PineGreen else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          )
        }
      }

      // 3. Tab Contents
      if (selectedTab == 0) {
        if (activeListings.isEmpty()) {
          item {
            EmptyListingsCard(
              title = "No active listings",
              subtitle = "List your pre-loved fashion, tech, or vintage goods to start earning.",
              buttonText = "+ Create First Listing",
              onAction = { viewModel.navigateTo(Screen.SELL_ITEM) }
            )
          }
        } else {
          items(
            items = activeListings,
            key = { it.id }
          ) { product ->
            SellerListingCard(
              product = product,
              isSoldTab = false,
              onToggleSold = { viewModel.toggleSoldStatus(product) },
              onDelete = { viewModel.deleteListing(product.id) },
              onClick = { viewModel.openProductDetail(product) }
            )
          }
        }
      } else {
        if (soldListings.isEmpty()) {
          item {
            EmptyListingsCard(
              title = "No sold items yet",
              subtitle = "When buyers purchase your items via checkout, they will appear here.",
              buttonText = "View Active Listings",
              onAction = { selectedTab = 0 }
            )
          }
        } else {
          items(
            items = soldListings,
            key = { it.id }
          ) { product ->
            SellerListingCard(
              product = product,
              isSoldTab = true,
              onToggleSold = { viewModel.toggleSoldStatus(product) },
              onDelete = { viewModel.deleteListing(product.id) },
              onClick = { viewModel.openProductDetail(product) }
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }

    // FAB: Add Listing
    FloatingActionButton(
      onClick = { viewModel.navigateTo(Screen.SELL_ITEM) },
      containerColor = MaterialTheme.colorScheme.secondary,
      contentColor = Color.White,
      shape = CircleShape,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("fab_create_listing")
    ) {
      Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text("List Item", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun SellerListingCard(
  product: ProductEntity,
  isSoldTab: Boolean,
  onToggleSold: () -> Unit,
  onDelete: () -> Unit,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(product.imageUrl)
          .crossfade(true)
          .placeholder(R.drawable.marketplace_hero)
          .error(R.drawable.marketplace_hero)
          .build(),
        contentDescription = product.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(76.dp)
          .clip(RoundedCornerShape(10.dp))
          .clickable { onClick() }
      )

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Text(
            text = product.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
              .weight(1f)
              .clickable { onClick() }
          )

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "$${String.format("%.2f", product.price)}",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (isSoldTab) Terracotta.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
          ) {
            Text(
              text = if (isSoldTab) "SOLD" else product.condition,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSoldTab) Terracotta else PineGreen,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Button: Mark Sold or Relist
        OutlinedButton(
          onClick = onToggleSold,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(32.dp)
        ) {
          Icon(
            imageVector = if (isSoldTab) Icons.Default.Replay else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isSoldTab) PineGreen else Terracotta,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isSoldTab) "Relist Item" else "Mark as Sold",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSoldTab) PineGreen else Terracotta
          )
        }
      }
    }
  }
}

@Composable
private fun StatColumn(value: String, label: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      fontWeight = FontWeight.Bold,
      fontSize = 16.sp,
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = label,
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun EmptyListingsCard(
  title: String,
  subtitle: String,
  buttonText: String,
  onAction: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 24.dp)
  ) {
    Column(
      modifier = Modifier.padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = "🏷️", fontSize = 36.sp)
      Spacer(modifier = Modifier.height(10.dp))
      Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = subtitle,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
      Spacer(modifier = Modifier.height(16.dp))
      Button(
        onClick = onAction,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Text(buttonText, fontWeight = FontWeight.Bold)
      }
    }
  }
}
