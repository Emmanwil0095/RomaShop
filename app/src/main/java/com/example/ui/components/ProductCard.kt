package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.ProductEntity
import com.example.ui.theme.PineGreen
import com.example.ui.theme.SageGreen
import com.example.ui.theme.Terracotta

@Composable
fun ProductCard(
  product: ProductEntity,
  isFavorite: Boolean,
  onCardClick: () -> Unit,
  onFavoriteClick: () -> Unit,
  onAddToCartClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
      .testTag("product_card_${product.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column {
      // Image Container with Badges & Favorite
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1f)
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

        // Condition badge (Top-Left)
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
          modifier = Modifier
            .padding(8.dp)
            .align(Alignment.TopStart)
        ) {
          Text(
            text = product.condition,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = PineGreen,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }

        // Sold Overlay if sold
        if (product.isSold) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Terracotta
            ) {
              Text(
                text = "SOLD",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }

        // Favorite Button (Top-Right)
        IconButton(
          onClick = onFavoriteClick,
          modifier = Modifier
            .padding(6.dp)
            .size(34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
            .align(Alignment.TopEnd)
            .testTag("favorite_button_${product.id}")
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Save Item",
            tint = if (isFavorite) Terracotta else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }

        // Discount Tag (Bottom-Left)
        if (product.originalPrice > product.price) {
          val discount = (((product.originalPrice - product.price) / product.originalPrice) * 100).toInt()
          if (discount > 5) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Terracotta,
              modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomStart)
            ) {
              Text(
                text = "-$discount%",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      }

      // Details Block
      Column(
        modifier = Modifier.padding(12.dp)
      ) {
        // Price Row
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "$${String.format("%.2f", product.price)}",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (product.originalPrice > product.price) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "$${String.format("%.2f", product.originalPrice)}",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textDecoration = TextDecoration.LineThrough
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Title
        Text(
          text = product.title,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Seller and Rating
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = product.sellerName,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFF4A261),
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(
            text = String.format("%.1f", product.sellerRating),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Eco impact & Quick Add
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "🌱",
              fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "-${String.format("%.1f", product.co2SavedKg)}kg CO2",
              fontSize = 10.sp,
              color = SageGreen,
              fontWeight = FontWeight.SemiBold
            )
          }

          if (!product.isSold) {
            IconButton(
              onClick = onAddToCartClick,
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .testTag("add_cart_${product.id}")
            ) {
              Icon(
                imageVector = Icons.Default.AddShoppingCart,
                contentDescription = "Add to Cart",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }
    }
  }
}
