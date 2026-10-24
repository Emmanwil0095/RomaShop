package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CartItemWithProduct
import com.example.data.model.OrderEntity
import com.example.data.model.OrderWithItems
import com.example.data.model.ProductEntity
import com.example.data.model.UserEntity
import com.example.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
  EXPLORE,
  PRODUCT_DETAIL,
  CART,
  CHECKOUT,
  CHECKOUT_SUCCESS,
  SELL_ITEM,
  SELLER_DASHBOARD,
  ORDERS,
  PROFILE
}

enum class SortOption(val label: String) {
  NEWEST("Newest First"),
  PRICE_LOW("Price: Low to High"),
  PRICE_HIGH("Price: High to Low")
}

sealed class PaymentUiState {
  object Idle : PaymentUiState()
  object CreatingPaymentIntent : PaymentUiState()
  data class ProcessingPayment(val intentId: String) : PaymentUiState()
  data class Success(val order: OrderEntity, val transactionId: String) : PaymentUiState()
  data class Error(val message: String, val declineCode: String?) : PaymentUiState()
}

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application, viewModelScope)
  val repository = MarketplaceRepository(database)

  // Current session user
  val currentUser: StateFlow<UserEntity> = repository.currentUser

  // Navigation
  private val _currentScreen = MutableStateFlow(Screen.EXPLORE)
  val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

  private val _screenBackStack = mutableListOf<Screen>()

  // Product Detail Selection
  private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
  val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

  // Last completed order (for order confirmation screen)
  private val _lastPlacedOrder = MutableStateFlow<OrderEntity?>(null)
  val lastPlacedOrder: StateFlow<OrderEntity?> = _lastPlacedOrder.asStateFlow()

  // Selected Order for detail
  private val _selectedOrderDetails = MutableStateFlow<OrderWithItems?>(null)
  val selectedOrderDetails: StateFlow<OrderWithItems?> = _selectedOrderDetails.asStateFlow()

  // Search, Category & Sorting Filters
  val searchQuery = MutableStateFlow("")
  val selectedCategory = MutableStateFlow("All")
  val selectedSort = MutableStateFlow(SortOption.NEWEST)

  // Favorites
  private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
  val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

  // Status message (snackbars)
  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  // Filtered & Sorted Products
  val products: StateFlow<List<ProductEntity>> = combine(
    selectedCategory.flatMapLatest { cat -> repository.getProductsByCategory(cat) },
    searchQuery,
    selectedSort
  ) { list, query, sort ->
    var result = list
    if (query.isNotBlank()) {
      val q = query.trim().lowercase()
      result = result.filter {
        it.title.lowercase().contains(q) ||
          it.description.lowercase().contains(q) ||
          it.category.lowercase().contains(q) ||
          it.sellerName.lowercase().contains(q)
      }
    }
    when (sort) {
      SortOption.NEWEST -> result.sortedByDescending { it.createdAt }
      SortOption.PRICE_LOW -> result.sortedBy { it.price }
      SortOption.PRICE_HIGH -> result.sortedByDescending { it.price }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Cart Flow
  val cartItems: StateFlow<List<CartItemWithProduct>> = currentUser.flatMapLatest { user ->
    repository.getCartWithProducts(user.id)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val cartCount: StateFlow<Int> = cartItems.combine(cartItems) { items, _ ->
    items.sumOf { it.cartItem.quantity }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val cartSubtotal: StateFlow<Double> = cartItems.combine(cartItems) { items, _ ->
    items.sumOf { it.product.price * it.cartItem.quantity }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  // Seller Dashboard Products
  val sellerActiveListings: StateFlow<List<ProductEntity>> = currentUser.flatMapLatest { user ->
    repository.getSellerProducts(user.id)
  }.combine(currentUser) { list, _ ->
    list.filter { !it.isSold }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val sellerSoldListings: StateFlow<List<ProductEntity>> = currentUser.flatMapLatest { user ->
    repository.getSellerProducts(user.id)
  }.combine(currentUser) { list, _ ->
    list.filter { it.isSold }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // User Orders
  val userOrders: StateFlow<List<OrderEntity>> = currentUser.flatMapLatest { user ->
    repository.getOrdersForUser(user.id)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Navigation handlers
  fun navigateTo(screen: Screen) {
    if (_currentScreen.value != screen) {
      _screenBackStack.add(_currentScreen.value)
      _currentScreen.value = screen
    }
  }

  fun navigateBack(): Boolean {
    if (_screenBackStack.isNotEmpty()) {
      _currentScreen.value = _screenBackStack.removeAt(_screenBackStack.size - 1)
      return true
    }
    if (_currentScreen.value != Screen.EXPLORE) {
      _currentScreen.value = Screen.EXPLORE
      return true
    }
    return false
  }

  fun openProductDetail(product: ProductEntity) {
    _selectedProduct.value = product
    navigateTo(Screen.PRODUCT_DETAIL)
  }

  fun toggleFavorite(productId: String) {
    val current = _favoriteIds.value
    if (productId in current) {
      _favoriteIds.value = current - productId
      showSnackbar("Removed from saved items")
    } else {
      _favoriteIds.value = current + productId
      showSnackbar("Saved to wishlist ♥")
    }
  }

  // Cart actions
  fun addToCart(product: ProductEntity) {
    viewModelScope.launch {
      repository.addToCart(currentUser.value.id, product.id)
      showSnackbar("Added '${product.title}' to cart!")
    }
  }

  fun buyNow(product: ProductEntity) {
    viewModelScope.launch {
      repository.addToCart(currentUser.value.id, product.id)
      navigateTo(Screen.CART)
    }
  }

  fun updateCartQuantity(cartItemId: String, quantity: Int) {
    viewModelScope.launch {
      repository.updateCartQuantity(cartItemId, quantity)
    }
  }

  fun removeCartItem(cartItemId: String) {
    viewModelScope.launch {
      repository.removeFromCart(cartItemId)
      showSnackbar("Item removed from cart")
    }
  }

  fun clearCart() {
    viewModelScope.launch {
      repository.clearCart(currentUser.value.id)
    }
  }

  // Payment Gateway
  val paymentGateway = com.example.data.payment.StripePaymentGateway()

  private val _paymentUiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
  val paymentUiState: StateFlow<PaymentUiState> = _paymentUiState.asStateFlow()

  fun resetPaymentState() {
    _paymentUiState.value = PaymentUiState.Idle
  }

  // Stripe Card Checkout Action with Payment Intent & Callbacks
  fun processStripePayment(
    buyerName: String,
    buyerEmail: String,
    shippingAddress: String,
    card: com.example.data.payment.CardDetails
  ) {
    val items = cartItems.value
    if (items.isEmpty()) return

    val subtotal = items.sumOf { it.product.price * it.cartItem.quantity }
    val shippingFee = if (subtotal >= 50.0 || subtotal == 0.0) 0.0 else 4.99
    val totalAmount = subtotal + shippingFee

    viewModelScope.launch {
      try {
        _paymentUiState.value = PaymentUiState.CreatingPaymentIntent
        val intent = paymentGateway.createPaymentIntent(totalAmount)

        _paymentUiState.value = PaymentUiState.ProcessingPayment(intent.id)
        val result = paymentGateway.confirmPayment(intent.id, card)

        when (result) {
          is com.example.data.payment.PaymentResult.Success -> {
            val maskedMethod = "Stripe (${result.cardBrand} •••• ${result.last4})"
            val order = repository.placeOrder(
              userId = currentUser.value.id,
              buyerName = buyerName,
              buyerEmail = buyerEmail,
              shippingAddress = shippingAddress,
              paymentMethod = maskedMethod,
              paymentIntentId = result.paymentIntentId,
              transactionStatus = "Succeeded (Txn: ${result.transactionId})",
              itemsWithProducts = items
            )
            _lastPlacedOrder.value = order
            _paymentUiState.value = PaymentUiState.Success(order, result.transactionId)
            navigateTo(Screen.CHECKOUT_SUCCESS)
            showSnackbar("Payment of $${String.format("%.2f", totalAmount)} authorized via Stripe!")
          }
          is com.example.data.payment.PaymentResult.Failure -> {
            _paymentUiState.value = PaymentUiState.Error(result.errorMessage, result.declineCode)
            showSnackbar(result.errorMessage)
          }
          is com.example.data.payment.PaymentResult.Canceled -> {
            _paymentUiState.value = PaymentUiState.Idle
            showSnackbar("Payment flow was canceled.")
          }
        }
      } catch (e: Exception) {
        _paymentUiState.value = PaymentUiState.Error(e.message ?: "Payment processing error", null)
        showSnackbar(e.message ?: "An unexpected error occurred during payment.")
      }
    }
  }

  // Alternative payment checkout (e.g. Cash on Delivery / Google Pay)
  fun checkout(
    buyerName: String,
    buyerEmail: String,
    shippingAddress: String,
    paymentMethod: String
  ) {
    val items = cartItems.value
    if (items.isEmpty()) return

    viewModelScope.launch {
      val order = repository.placeOrder(
        userId = currentUser.value.id,
        buyerName = buyerName,
        buyerEmail = buyerEmail,
        shippingAddress = shippingAddress,
        paymentMethod = paymentMethod,
        paymentIntentId = "non_stripe_" + java.util.UUID.randomUUID().toString().take(8),
        transactionStatus = "Pending Verification",
        itemsWithProducts = items
      )
      _lastPlacedOrder.value = order
      navigateTo(Screen.CHECKOUT_SUCCESS)
      showSnackbar("Order #${order.orderNumber} placed successfully!")
    }
  }

  // Seller Listing Actions
  fun createListing(
    title: String,
    description: String,
    price: Double,
    originalPrice: Double,
    category: String,
    condition: String,
    imageUrl: String
  ) {
    viewModelScope.launch {
      val created = repository.createProduct(
        title = title,
        description = description,
        price = price,
        originalPrice = originalPrice,
        category = category,
        condition = condition,
        imageUrl = imageUrl
      )
      showSnackbar("Listing published for '${created.title}'!")
      navigateTo(Screen.SELLER_DASHBOARD)
    }
  }

  fun toggleSoldStatus(product: ProductEntity) {
    viewModelScope.launch {
      val newStatus = !product.isSold
      repository.markAsSold(product.id, newStatus)
      showSnackbar(if (newStatus) "Marked as Sold!" else "Item relisted as Active!")
    }
  }

  fun deleteListing(productId: String) {
    viewModelScope.launch {
      repository.deleteProduct(productId)
      showSnackbar("Listing deleted")
    }
  }

  // JWT Token & Auth Actions
  val jwtToken: StateFlow<String> = repository.jwtToken

  val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    emptyList()
  )

  fun login(
    emailOrUsername: String,
    pass: String,
    onResult: (Boolean, String) -> Unit
  ) {
    viewModelScope.launch {
      val result = repository.login(emailOrUsername, pass)
      when (result) {
        is com.example.data.auth.AuthResult.Success -> {
          showSnackbar("Welcome back, ${result.session.user.name}!")
          onResult(true, "Login successful")
        }
        is com.example.data.auth.AuthResult.Error -> {
          onResult(false, result.message)
        }
      }
    }
  }

  fun register(
    username: String,
    email: String,
    pass: String,
    name: String,
    isSeller: Boolean,
    bio: String,
    onResult: (Boolean, String) -> Unit
  ) {
    viewModelScope.launch {
      val result = repository.register(username, email, pass, name, isSeller, bio)
      when (result) {
        is com.example.data.auth.AuthResult.Success -> {
          showSnackbar("Account created! Signed JWT token issued.")
          onResult(true, "Registration successful")
        }
        is com.example.data.auth.AuthResult.Error -> {
          onResult(false, result.message)
        }
      }
    }
  }

  fun switchUser(user: UserEntity) {
    viewModelScope.launch {
      repository.setCurrentUser(user)
      showSnackbar("Switched active profile to ${user.name}")
    }
  }

  fun updateProfile(name: String, bio: String, isSeller: Boolean) {
    viewModelScope.launch {
      val updated = repository.authService.updateProfile(currentUser.value.id, name, bio, isSeller)
      if (updated != null) {
        repository.setCurrentUser(updated)
        showSnackbar("Profile updated successfully")
      }
    }
  }

  // Account / User Actions
  fun createAccount(name: String, email: String, bio: String, isSeller: Boolean) {
    viewModelScope.launch {
      val user = repository.createAccount(name, email, bio, isSeller)
      showSnackbar("Welcome to Relove, ${user.name}!")
      navigateTo(Screen.EXPLORE)
    }
  }

  fun showSnackbar(message: String) {
    _snackbarMessage.value = message
  }

  fun clearSnackbar() {
    _snackbarMessage.value = null
  }
}
