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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.CartItemWithProduct
import com.example.ui.theme.PineGreen
import com.example.ui.theme.SageGreen
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.MarketplaceViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CartScreen(
  viewModel: MarketplaceViewModel,
  modifier: Modifier = Modifier
) {
  val cartItems by viewModel.cartItems.collectAsState()
  val subtotal by viewModel.cartSubtotal.collectAsState()

  val freeShippingThreshold = 50.0
  val shippingFee = if (subtotal >= freeShippingThreshold || subtotal == 0.0) 0.0 else 4.99
  val total = subtotal + shippingFee
  val totalEcoKg = cartItems.sumOf { it.product.co2SavedKg * it.cartItem.quantity }

  if (cartItems.isEmpty()) {
    EmptyCartView(onStartShopping = { viewModel.navigateTo(Screen.EXPLORE) }, modifier = modifier)
    return
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(6.dp))
        // Free shipping progress
        FreeShippingProgress(subtotal = subtotal, threshold = freeShippingThreshold)
      }

      items(
        items = cartItems,
        key = { it.cartItem.id }
      ) { itemWithProduct ->
        CartItemRow(
          item = itemWithProduct,
          onQuantityChange = { newQty ->
            viewModel.updateCartQuantity(itemWithProduct.cartItem.id, newQty)
          },
          onRemove = {
            viewModel.removeCartItem(itemWithProduct.cartItem.id)
          },
          onClick = {
            viewModel.openProductDetail(itemWithProduct.product)
          }
        )
      }

      item {
        // Cost Breakdown Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Order Summary",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryLine("Subtotal (${cartItems.sumOf { it.cartItem.quantity }} items)", "$${String.format("%.2f", subtotal)}")
            SummaryLine("Shipping (Carbon-Neutral)", if (shippingFee == 0.0) "FREE" else "$${String.format("%.2f", shippingFee)}")

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Total",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "$${String.format("%.2f", total)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = PineGreen
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Eco badge
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = "🌱", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Saving ~${String.format("%.1f", totalEcoKg)}kg CO2 emissions with this secondhand order!",
                  fontSize = 11.sp,
                  color = PineGreen,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(100.dp))
      }
    }

    // Bottom Sticky Checkout Bar
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Total Amount",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "$${String.format("%.2f", total)}",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Button(
          onClick = { viewModel.navigateTo(Screen.CHECKOUT) },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
          modifier = Modifier
            .height(50.dp)
            .testTag("checkout_button")
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Proceed to Checkout",
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }
  }
}

@Composable
private fun CartItemRow(
  item: CartItemWithProduct,
  onQuantityChange: (Int) -> Unit,
  onRemove: () -> Unit,
  onClick: () -> Unit
) {
  val product = item.product
  val cartItem = item.cartItem

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("cart_item_${cartItem.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Thumbnail
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
          .size(80.dp)
          .clip(RoundedCornerShape(12.dp))
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
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
              .weight(1f)
              .clickable { onClick() }
          )

          IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Remove",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
          shape = RoundedCornerShape(4.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
          Text(
            text = product.condition,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = PineGreen,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Price & Stepper
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "$${String.format("%.2f", product.price)}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          // Stepper
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(2.dp)
          ) {
            IconButton(
              onClick = { onQuantityChange(cartItem.quantity - 1) },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp)
              )
            }

            Text(
              text = cartItem.quantity.toString(),
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
              onClick = { onQuantityChange(cartItem.quantity + 1) },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun FreeShippingProgress(subtotal: Double, threshold: Double) {
  val remaining = threshold - subtotal
  val progress = (subtotal / threshold).coerceIn(0.0, 1.0).toFloat()

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "📦", fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (remaining <= 0) "You unlocked FREE carbon-neutral shipping!" else "Add $${String.format("%.2f", remaining)} more for FREE shipping",
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = if (remaining <= 0) PineGreen else MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = PineGreen,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )
    }
  }
}

@Composable
private fun SummaryLine(title: String, amount: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = title,
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = amount,
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun EmptyCartView(onStartShopping: () -> Unit, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(32.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(90.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.ShoppingBag,
        contentDescription = null,
        tint = PineGreen,
        modifier = Modifier.size(44.dp)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Your Cart is Empty",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Discover unique vintage clothes, electronics, home decor, and collectibles from verified sellers.",
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      lineHeight = 18.sp
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = onStartShopping,
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
      modifier = Modifier.testTag("start_shopping_button")
    ) {
      Text(
        text = "Explore Secondhand Goods",
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
      )
    }
  }
}
