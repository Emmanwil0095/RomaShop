package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.MarketplaceBottomNav
import com.example.ui.components.MarketplaceTopBar
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.CheckoutSuccessScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SellerDashboardScreen
import com.example.ui.screens.SellItemScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MarketplaceViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainAppContent()
      }
    }
  }
}

@Composable
fun MainAppContent(viewModel: MarketplaceViewModel = viewModel()) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val cartCount by viewModel.cartCount.collectAsState()
  val snackbarMessage by viewModel.snackbarMessage.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(snackbarMessage) {
    snackbarMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearSnackbar()
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    topBar = {
      MarketplaceTopBar(
        currentScreen = currentScreen,
        cartCount = cartCount,
        onNavigateBack = { viewModel.navigateBack() },
        onNavigateToCart = { viewModel.navigateTo(Screen.CART) },
        onNavigateToSell = { viewModel.navigateTo(Screen.SELL_ITEM) }
      )
    },
    bottomBar = {
      MarketplaceBottomNav(
        currentScreen = currentScreen,
        cartCount = cartCount,
        onNavigate = { screen -> viewModel.navigateTo(screen) }
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (currentScreen) {
        Screen.EXPLORE -> ExploreScreen(viewModel = viewModel)
        Screen.PRODUCT_DETAIL -> ProductDetailScreen(viewModel = viewModel)
        Screen.CART -> CartScreen(viewModel = viewModel)
        Screen.CHECKOUT -> CheckoutScreen(viewModel = viewModel)
        Screen.CHECKOUT_SUCCESS -> CheckoutSuccessScreen(viewModel = viewModel)
        Screen.SELL_ITEM -> SellItemScreen(viewModel = viewModel)
        Screen.SELLER_DASHBOARD -> SellerDashboardScreen(viewModel = viewModel)
        Screen.ORDERS -> OrdersScreen(viewModel = viewModel)
        Screen.PROFILE -> ProfileScreen(viewModel = viewModel)
      }
    }
  }
}
