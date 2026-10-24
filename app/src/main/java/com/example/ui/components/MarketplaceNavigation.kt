package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceTopBar(
  currentScreen: Screen,
  cartCount: Int,
  onNavigateBack: () -> Unit,
  onNavigateToCart: () -> Unit,
  onNavigateToSell: () -> Unit
) {
  val showBack = currentScreen != Screen.EXPLORE &&
    currentScreen != Screen.SELLER_DASHBOARD &&
    currentScreen != Screen.PROFILE &&
    currentScreen != Screen.ORDERS

  TopAppBar(
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!showBack) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_app_logo),
              contentDescription = "Relove Logo",
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
        }

        Text(
          text = when (currentScreen) {
            Screen.EXPLORE -> "Relove"
            Screen.PRODUCT_DETAIL -> "Item Details"
            Screen.CART -> "Your Cart"
            Screen.CHECKOUT -> "Checkout"
            Screen.CHECKOUT_SUCCESS -> "Order Placed"
            Screen.SELL_ITEM -> "List an Item"
            Screen.SELLER_DASHBOARD -> "Seller Dashboard"
            Screen.ORDERS -> "Your Orders"
            Screen.PROFILE -> "Profile & Eco Hub"
          },
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    },
    navigationIcon = {
      if (showBack) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier.testTag("topbar_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    },
    actions = {
      if (currentScreen != Screen.SELL_ITEM) {
        IconButton(
          onClick = onNavigateToSell,
          modifier = Modifier.testTag("topbar_sell_button")
        ) {
          Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = "Sell Item",
            tint = MaterialTheme.colorScheme.secondary
          )
        }
      }

      if (currentScreen != Screen.CART && currentScreen != Screen.CHECKOUT && currentScreen != Screen.CHECKOUT_SUCCESS) {
        IconButton(
          onClick = onNavigateToCart,
          modifier = Modifier.testTag("topbar_cart_button")
        ) {
          BadgedBox(
            badge = {
              if (cartCount > 0) {
                Badge(
                  containerColor = MaterialTheme.colorScheme.secondary,
                  contentColor = Color.White
                ) {
                  Text(text = cartCount.toString())
                }
              }
            }
          ) {
            Icon(
              imageVector = Icons.Outlined.ShoppingBag,
              contentDescription = "Cart",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  )
}

@Composable
fun MarketplaceBottomNav(
  currentScreen: Screen,
  cartCount: Int,
  onNavigate: (Screen) -> Unit
) {
  Surface(
    shadowElevation = 8.dp,
    color = MaterialTheme.colorScheme.surface
  ) {
    NavigationBar(
      containerColor = MaterialTheme.colorScheme.surface,
      tonalElevation = 0.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      NavigationBarItem(
        selected = currentScreen == Screen.EXPLORE || currentScreen == Screen.PRODUCT_DETAIL,
        onClick = { onNavigate(Screen.EXPLORE) },
        icon = {
          Icon(
            imageVector = if (currentScreen == Screen.EXPLORE) Icons.Filled.Search else Icons.Outlined.Search,
            contentDescription = "Explore"
          )
        },
        label = { Text("Explore") },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.primary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.testTag("nav_explore")
      )

      NavigationBarItem(
        selected = currentScreen == Screen.SELLER_DASHBOARD || currentScreen == Screen.SELL_ITEM,
        onClick = { onNavigate(Screen.SELLER_DASHBOARD) },
        icon = {
          Icon(
            imageVector = if (currentScreen == Screen.SELLER_DASHBOARD) Icons.Filled.Storefront else Icons.Outlined.Storefront,
            contentDescription = "Sell"
          )
        },
        label = { Text("My Shop") },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.primary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.testTag("nav_seller")
      )

      NavigationBarItem(
        selected = currentScreen == Screen.CART || currentScreen == Screen.CHECKOUT,
        onClick = { onNavigate(Screen.CART) },
        icon = {
          BadgedBox(
            badge = {
              if (cartCount > 0) {
                Badge(
                  containerColor = MaterialTheme.colorScheme.secondary,
                  contentColor = Color.White
                ) {
                  Text(text = cartCount.toString())
                }
              }
            }
          ) {
            Icon(
              imageVector = if (currentScreen == Screen.CART) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
              contentDescription = "Cart"
            )
          }
        },
        label = { Text("Cart") },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.primary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.testTag("nav_cart")
      )

      NavigationBarItem(
        selected = currentScreen == Screen.ORDERS,
        onClick = { onNavigate(Screen.ORDERS) },
        icon = {
          Icon(
            imageVector = if (currentScreen == Screen.ORDERS) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
            contentDescription = "Orders"
          )
        },
        label = { Text("Orders") },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.primary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.testTag("nav_orders")
      )

      NavigationBarItem(
        selected = currentScreen == Screen.PROFILE,
        onClick = { onNavigate(Screen.PROFILE) },
        icon = {
          Icon(
            imageVector = if (currentScreen == Screen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
            contentDescription = "Profile"
          )
        },
        label = { Text("Profile") },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.primary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.testTag("nav_profile")
      )
    }
  }
}
