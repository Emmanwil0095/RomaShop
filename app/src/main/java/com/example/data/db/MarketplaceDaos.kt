package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CartItemEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.ProductEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM users")
  fun getAllUsers(): Flow<List<UserEntity>>

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  suspend fun getUserById(id: String): UserEntity?

  @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
  suspend fun getUserByEmail(email: String): UserEntity?

  @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
  suspend fun getUserByUsername(username: String): UserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllUsers(users: List<UserEntity>)

  @Update
  suspend fun updateUser(user: UserEntity)
}

@Dao
interface ProductDao {
  @Query("SELECT * FROM products ORDER BY createdAt DESC")
  fun getAllProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE isSold = 0 ORDER BY createdAt DESC")
  fun getActiveProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
  suspend fun getProductById(id: String): ProductEntity?

  @Query("SELECT * FROM products WHERE isSold = 0 AND category = :category ORDER BY createdAt DESC")
  fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE sellerId = :sellerId ORDER BY createdAt DESC")
  fun getProductsBySeller(sellerId: String): Flow<List<ProductEntity>>

  @Query("""
    SELECT * FROM products 
    WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
    ORDER BY createdAt DESC
  """)
  fun searchProducts(query: String): Flow<List<ProductEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllProducts(products: List<ProductEntity>)

  @Update
  suspend fun updateProduct(product: ProductEntity)

  @Query("UPDATE products SET isSold = :isSold WHERE id = :productId")
  suspend fun updateSoldStatus(productId: String, isSold: Boolean)

  @Delete
  suspend fun deleteProduct(product: ProductEntity)

  @Query("DELETE FROM products WHERE id = :productId")
  suspend fun deleteProductById(productId: String)
}

@Dao
interface CartDao {
  @Query("SELECT * FROM cart_items WHERE userId = :userId ORDER BY addedAt DESC")
  fun getCartItems(userId: String): Flow<List<CartItemEntity>>

  @Query("SELECT * FROM cart_items WHERE id = :id LIMIT 1")
  suspend fun getCartItemById(id: String): CartItemEntity?

  @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
  suspend fun getCartItem(userId: String, productId: String): CartItemEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCartItem(cartItem: CartItemEntity)

  @Update
  suspend fun updateCartItem(cartItem: CartItemEntity)

  @Delete
  suspend fun removeCartItem(cartItem: CartItemEntity)

  @Query("DELETE FROM cart_items WHERE id = :cartItemId")
  suspend fun removeCartItemById(cartItemId: String)

  @Query("DELETE FROM cart_items WHERE userId = :userId")
  suspend fun clearCart(userId: String)
}

@Dao
interface OrderDao {
  @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
  fun getOrdersByUser(userId: String): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders ORDER BY createdAt DESC")
  fun getAllOrders(): Flow<List<OrderEntity>>

  @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
  suspend fun getOrderById(orderId: String): OrderEntity?

  @Query("SELECT * FROM order_items WHERE orderId = :orderId")
  suspend fun getItemsForOrder(orderId: String): List<OrderItemEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: OrderEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrderItems(items: List<OrderItemEntity>)
}
