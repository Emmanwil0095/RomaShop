package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val username: String = "user",
  val name: String,
  val email: String,
  val passwordHash: String = "",
  val salt: String = "",
  val avatarUrl: String = "",
  val bio: String = "",
  val isSeller: Boolean = true,
  val rating: Float = 4.9f,
  val salesCount: Int = 12,
  val joinedDate: String = "Joined 2024"
)

@Entity(tableName = "products")
data class ProductEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val title: String,
  val description: String,
  val price: Double,
  val originalPrice: Double = price * 1.5,
  val category: String,
  val condition: String, // "Brand New", "Like New", "Good", "Fair"
  val imageUrl: String,
  val sellerId: String,
  val sellerName: String,
  val sellerRating: Float = 4.8f,
  val isSold: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val co2SavedKg: Double = 2.8
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val productId: String,
  val userId: String,
  val quantity: Int = 1,
  val addedAt: Long = System.currentTimeMillis()
)

data class CartItemWithProduct(
  val cartItem: CartItemEntity,
  val product: ProductEntity
)

@Entity(tableName = "orders")
data class OrderEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val orderNumber: String,
  val userId: String,
  val buyerName: String,
  val buyerEmail: String,
  val shippingAddress: String,
  val paymentMethod: String,
  val paymentIntentId: String = "",
  val transactionStatus: String = "Succeeded",
  val subtotal: Double,
  val shippingFee: Double,
  val totalAmount: Double,
  val status: String, // "Confirmed", "Shipped", "Delivered"
  val itemCount: Int,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val orderId: String,
  val productId: String,
  val productTitle: String,
  val productPrice: Double,
  val productImageUrl: String,
  val quantity: Int
)

data class OrderWithItems(
  val order: OrderEntity,
  val items: List<OrderItemEntity>
)

object Categories {
  val ALL = listOf(
    "All",
    "Fashion",
    "Electronics",
    "Home & Living",
    "Vintage",
    "Books",
    "Collectibles"
  )
}

object Conditions {
  val ALL = listOf(
    "Like New",
    "Brand New",
    "Good",
    "Fair"
  )
}
