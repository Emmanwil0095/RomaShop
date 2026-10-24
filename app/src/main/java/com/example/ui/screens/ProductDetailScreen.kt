package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.theme.PineGreen
import com.example.ui.theme.SageGreen
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.MarketplaceViewModel

@Composable
fun ProductDetailScreen(
  viewModel: MarketplaceViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  val product = viewModel.selectedProduct.collectAsState().value
  val favoriteIds by viewModel.favoriteIds.collectAsState()

  if (product == null) {
    Box(
      modifier = modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text("Product not found")
    }
    return
  }

  val isFavorite = product.id in favoriteIds
  val scrollState = rememberScrollState()

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(bottom = 96.dp)
    ) {
      // 1. Large Image Header with Floating Badges
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1.15f)
          .background(MaterialTheme.colorScheme.surfaceVariant)
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
          modifier = Modifier.fillMaxSize()
        )

        // Sold banner if sold
        if (product.isSold) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
          ) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Terracotta
            ) {
              Text(
                text = "THIS ITEM HAS BEEN SOLD",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
              )
            }
          }
        }

        // Badges: Category & Condition (Top-Left)
        Row(
          modifier = Modifier
            .padding(16.dp)
            .align(Alignment.TopStart),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
          ) {
            Text(
              text = product.condition,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = PineGreen,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
          ) {
            Text(
              text = product.category,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        }

        // Favorite Button (Top-Right)
        IconButton(
          onClick = { viewModel.toggleFavorite(product.id) },
          modifier = Modifier
            .padding(16.dp)
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .align(Alignment.TopEnd)
            .testTag("detail_favorite_button")
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Save Item",
            tint = if (isFavorite) Terracotta else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      // 2. Main Details
      Column(
        modifier = Modifier.padding(20.dp)
      ) {
        // Price Row
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "$${String.format("%.2f", product.price)}",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
          )

          if (product.originalPrice > product.price) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "$${String.format("%.2f", product.originalPrice)}",
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textDecoration = TextDecoration.LineThrough
            )

            val savings = product.originalPrice - product.price
            val pct = ((savings / product.originalPrice) * 100).toInt()
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Terracotta.copy(alpha = 0.15f)
            ) {
              Text(
                text = "Save $pct% ($${String.format("%.0f", savings)})",
                color = Terracotta,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Text(
          text = product.title,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Sustainability Impact Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(PineGreen),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Circular Choice Impact",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PineGreen
              )
              Text(
                text = "Buying this secondhand item avoids ~${String.format("%.1f", product.co2SavedKg)}kg CO2 emissions and keeps quality materials out of landfills.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Description
        Text(
          text = "Description",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = product.description,
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Item Specifics Grid
        Text(
          text = "Item Details",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            DetailRow("Category", product.category)
            DetailRow("Condition", product.condition)
            DetailRow("Authentication", "Buyer Protection Guaranteed")
            DetailRow("Shipping", "Carbon-Neutral Delivery")
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Seller Card
        Text(
          text = "Meet the Seller",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(PineGreen),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = product.sellerName.take(2).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = product.sellerName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Verified,
                  contentDescription = "Verified Seller",
                  tint = PineGreen,
                  modifier = Modifier.size(16.dp)
                )
              }

              Spacer(modifier = Modifier.height(3.dp))

              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = null,
                  tint = Color(0xFFF4A261),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "${String.format("%.1f", product.sellerRating)} rating",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = " • 100% positive feedback",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }

    // 7. Sticky Bottom Action Bar
    Surface(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .navigationBarsPadding(),
      shadowElevation = 12.dp,
      color = MaterialTheme.colorScheme.surface
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = { viewModel.addToCart(product) },
          enabled = !product.isSold,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("detail_add_to_cart_button"),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        ) {
          Icon(
            imageVector = Icons.Outlined.ShoppingBag,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Add to Cart",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
        }

        Button(
          onClick = { viewModel.buyNow(product) },
          enabled = !product.isSold,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
          ),
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("detail_buy_now_button")
        ) {
          Text(
            text = if (product.isSold) "Sold Out" else "Buy Now",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
