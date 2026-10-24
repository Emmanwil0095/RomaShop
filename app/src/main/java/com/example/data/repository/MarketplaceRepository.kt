package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.CartItemEntity
import com.example.data.model.CartItemWithProduct
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.OrderWithItems
import com.example.data.model.ProductEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.util.UUID

class MarketplaceRepository(private val db: AppDatabase) {
  private val userDao = db.userDao()
  private val productDao = db.productDao()
  private val cartDao = db.cartDao()
  private val orderDao = db.orderDao()

  val authService = com.example.data.auth.AuthService(userDao)

  // Track the currently active session user and their active JWT token
  private val _currentUser = MutableStateFlow(
    UserEntity(
      id = "user_me",
      username = "alexmorgan",
      name = "Alex Morgan",
      email = "alex.morgan@relove.eco",
      avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
      bio = "Vintage enthusiast & sustainable lifestyle advocate.",
      isSeller = true,
      rating = 4.95f,
      salesCount = 28,
      joinedDate = "Member since March 2023"
    )
  )
  val currentUser = _currentUser.asStateFlow()

  private val _jwtToken = MutableStateFlow(authService.generateJwtToken(_currentUser.value))
  val jwtToken = _jwtToken.asStateFlow()

  // User methods
  fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

  suspend fun setCurrentUser(user: UserEntity) {
    _currentUser.value = user
    _jwtToken.value = authService.generateJwtToken(user)
    userDao.insertUser(user)
  }

  suspend fun login(emailOrUsername: String, pass: String): com.example.data.auth.AuthResult {
    val result = authService.login(emailOrUsername, pass)
    if (result is com.example.data.auth.AuthResult.Success) {
      _currentUser.value = result.session.user
      _jwtToken.value = result.session.token
    }
    return result
  }

  suspend fun register(
    username: String,
    email: String,
    pass: String,
    name: String,
    isSeller: Boolean,
    bio: String = ""
  ): com.example.data.auth.AuthResult {
    val result = authService.register(username, email, pass, name, isSeller, bio)
    if (result is com.example.data.auth.AuthResult.Success) {
      _currentUser.value = result.session.user
      _jwtToken.value = result.session.token
    }
    return result
  }

  suspend fun createAccount(name: String, email: String, bio: String, isSeller: Boolean): UserEntity {
    val newUser = UserEntity(
      id = "user_" + UUID.randomUUID().toString().take(8),
      name = name.trim(),
      email = email.trim(),
      avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=400&q=80",
      bio = bio.trim(),
      isSeller = isSeller,
      rating = 5.0f,
      salesCount = 0,
      joinedDate = "Member since today"
    )
    userDao.insertUser(newUser)
    _currentUser.value = newUser
    return newUser
  }

  suspend fun updateUser(user: UserEntity) {
    userDao.updateUser(user)
    if (_currentUser.value.id == user.id) {
      _currentUser.value = user
    }
  }

  // Product methods
  fun getAllActiveProducts(): Flow<List<ProductEntity>> = productDao.getActiveProducts()

  fun getProductsByCategory(category: String): Flow<List<ProductEntity>> {
    return if (category == "All") {
      productDao.getActiveProducts()
    } else {
      productDao.getProductsByCategory(category)
    }
  }

  fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

  fun getSellerProducts(sellerId: String): Flow<List<ProductEntity>> = productDao.getProductsBySeller(sellerId)

  suspend fun getProductById(id: String): ProductEntity? = productDao.getProductById(id)

  suspend fun createProduct(
    title: String,
    description: String,
    price: Double,
    originalPrice: Double,
    category: String,
    condition: String,
    imageUrl: String
  ): ProductEntity {
    val user = _currentUser.value
    val newProduct = ProductEntity(
      id = "prod_" + UUID.randomUUID().toString().take(8),
      title = title.trim(),
      description = description.trim(),
      price = price,
      originalPrice = if (originalPrice > price) originalPrice else price * 1.4,
      category = category,
      condition = condition,
      imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=80" },
      sellerId = user.id,
      sellerName = user.name,
      sellerRating = user.rating,
      isSold = false,
      createdAt = System.currentTimeMillis(),
      co2SavedKg = (price * 0.05).coerceAtLeast(1.5)
    )
    productDao.insertProduct(newProduct)

    // Update user's listing count/role
    if (!user.isSeller) {
      val updatedUser = user.copy(isSeller = true)
      updateUser(updatedUser)
    }
    return newProduct
  }

  suspend fun updateProduct(product: ProductEntity) {
    productDao.updateProduct(product)
  }

  suspend fun markAsSold(productId: String, isSold: Boolean) {
    productDao.updateSoldStatus(productId, isSold)
  }

  suspend fun deleteProduct(productId: String) {
    productDao.deleteProductById(productId)
  }

  // Cart methods
  fun getCartWithProducts(userId: String): Flow<List<CartItemWithProduct>> {
    return combine(cartDao.getCartItems(userId), productDao.getAllProducts()) { cartItems, allProducts ->
      val productMap = allProducts.associateBy { it.id }
      cartItems.mapNotNull { cartItem ->
        productMap[cartItem.productId]?.let { product ->
          CartItemWithProduct(cartItem, product)
        }
      }
    }
  }

  suspend fun addToCart(userId: String, productId: String) {
    val existing = cartDao.getCartItem(userId, productId)
    if (existing != null) {
      cartDao.updateCartItem(existing.copy(quantity = existing.quantity + 1))
    } else {
      cartDao.insertCartItem(CartItemEntity(productId = productId, userId = userId, quantity = 1))
    }
  }

  suspend fun updateCartQuantity(cartItemId: String, newQuantity: Int) {
    if (newQuantity <= 0) {
      cartDao.removeCartItemById(cartItemId)
    } else {
      val existing = cartDao.getCartItemById(cartItemId)
      if (existing != null) {
        cartDao.updateCartItem(existing.copy(quantity = newQuantity))
      }
    }
  }

  suspend fun removeFromCart(cartItemId: String) {
    cartDao.removeCartItemById(cartItemId)
  }

  suspend fun clearCart(userId: String) {
    cartDao.clearCart(userId)
  }

  // Orders methods
  fun getOrdersForUser(userId: String): Flow<List<OrderEntity>> = orderDao.getOrdersByUser(userId)

  suspend fun getOrderWithItems(orderId: String): OrderWithItems? {
    val order = orderDao.getOrderById(orderId) ?: return null
    val items = orderDao.getItemsForOrder(orderId)
    return OrderWithItems(order, items)
  }

  suspend fun placeOrder(
    userId: String,
    buyerName: String,
    buyerEmail: String,
    shippingAddress: String,
    paymentMethod: String,
    paymentIntentId: String = "",
    transactionStatus: String = "Succeeded",
    itemsWithProducts: List<CartItemWithProduct>
  ): OrderEntity {
    val subtotal = itemsWithProducts.sumOf { it.product.price * it.cartItem.quantity }
    val shippingFee = if (subtotal >= 50.0) 0.0 else 4.99
    val totalAmount = subtotal + shippingFee
    val totalItems = itemsWithProducts.sumOf { it.cartItem.quantity }

    val orderId = "ord_" + UUID.randomUUID().toString().take(8)
    val orderNumber = "RLV-" + (100000..999999).random()

    val newOrder = OrderEntity(
      id = orderId,
      orderNumber = orderNumber,
      userId = userId,
      buyerName = buyerName.trim(),
      buyerEmail = buyerEmail.trim(),
      shippingAddress = shippingAddress.trim(),
      paymentMethod = paymentMethod,
      paymentIntentId = paymentIntentId,
      transactionStatus = transactionStatus,
      subtotal = subtotal,
      shippingFee = shippingFee,
      totalAmount = totalAmount,
      status = "Confirmed",
      itemCount = totalItems,
      createdAt = System.currentTimeMillis()
    )

    val orderItems = itemsWithProducts.map { item ->
      OrderItemEntity(
        orderId = orderId,
        productId = item.product.id,
        productTitle = item.product.title,
        productPrice = item.product.price,
        productImageUrl = item.product.imageUrl,
        quantity = item.cartItem.quantity
      )
    }

    orderDao.insertOrder(newOrder)
    orderDao.insertOrderItems(orderItems)

    // Mark purchased secondhand products as sold (since secondhand items are unique one-offs!)
    for (item in itemsWithProducts) {
      productDao.updateSoldStatus(item.product.id, true)
    }

    // Clear cart
    cartDao.clearCart(userId)

    return newOrder
  }
}
