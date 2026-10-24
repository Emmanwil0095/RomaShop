package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CartItemEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.ProductEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    UserEntity::class,
    ProductEntity::class,
    CartItemEntity::class,
    OrderEntity::class,
    OrderItemEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun productDao(): ProductDao
  abstract fun cartDao(): CartDao
  abstract fun orderDao(): OrderDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "relove_marketplace_database"
        )
          .addCallback(MarketplaceDatabaseCallback(scope))
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }

  private class MarketplaceDatabaseCallback(
    private val scope: CoroutineScope
  ) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      INSTANCE?.let { database ->
        scope.launch(Dispatchers.IO) {
          populateInitialData(database)
        }
      }
    }

    private suspend fun populateInitialData(database: AppDatabase) {
      val userDao = database.userDao()
      val productDao = database.productDao()

      // Primary logged-in demo user & seller (demo password: password123)
      val currentUser = UserEntity(
        id = "user_me",
        username = "alexmorgan",
        name = "Alex Morgan",
        email = "alex.morgan@relove.eco",
        passwordHash = "4a1811693e506d87e07ffcf31c7908b8b3f2b1d7d0fa0402b8d009ec305a6669",
        salt = "salt_alex_123",
        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
        bio = "Vintage enthusiast & sustainable lifestyle advocate. Curating 80s & 90s pieces from London & Tokyo.",
        isSeller = true,
        rating = 4.95f,
        salesCount = 28,
        joinedDate = "Member since March 2023"
      )

      val sellerMaya = UserEntity(
        id = "user_maya",
        username = "mayathrift",
        name = "Maya Lin",
        email = "maya.thrift@gmail.com",
        passwordHash = "4a1811693e506d87e07ffcf31c7908b8b3f2b1d7d0fa0402b8d009ec305a6669",
        salt = "salt_maya_123",
        avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80",
        bio = "Audio tech collector, film cameras & mid-century furniture pieces.",
        isSeller = true,
        rating = 4.88f,
        salesCount = 45,
        joinedDate = "Member since Jan 2022"
      )

      val sellerLeo = UserEntity(
        id = "user_leo",
        username = "leovintage",
        name = "Leo Sterling",
        email = "leo.vintage@outlook.com",
        passwordHash = "4a1811693e506d87e07ffcf31c7908b8b3f2b1d7d0fa0402b8d009ec305a6669",
        salt = "salt_leo_123",
        avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
        bio = "Rare art books, mechanical watches, and vinyl records.",
        isSeller = true,
        rating = 4.92f,
        salesCount = 64,
        joinedDate = "Member since Oct 2021"
      )

      userDao.insertAllUsers(listOf(currentUser, sellerMaya, sellerLeo))

      // Initial curated secondhand products
      val seedProducts = listOf(
        ProductEntity(
          id = "prod_1",
          title = "Vintage 90s Oversized Denim Chore Jacket",
          description = "Authentic heavyweight Japanese denim jacket from the mid-90s with warm brass buttons and natural faded patina. Perfectly broken in, no tears or stains. Tagged Medium, fits relaxed.",
          price = 68.0,
          originalPrice = 145.0,
          category = "Fashion",
          condition = "Good",
          imageUrl = "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?auto=format&fit=crop&w=800&q=80",
          sellerId = "user_me",
          sellerName = "Alex Morgan",
          sellerRating = 4.95f,
          isSold = false,
          co2SavedKg = 4.2
        ),
        ProductEntity(
          id = "prod_2",
          title = "Canon AE-1 35mm SLR Film Camera + 50mm f/1.8",
          description = "Classic 1980s mechanical film camera. Shutter speeds tested and accurate, light seals replaced 2 months ago, pristine optics with zero fungus or haze. Comes with original leather neck strap.",
          price = 185.0,
          originalPrice = 320.0,
          category = "Electronics",
          condition = "Like New",
          imageUrl = "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?auto=format&fit=crop&w=800&q=80",
          sellerId = "user_maya",
          sellerName = "Maya Lin",
          sellerRating = 4.88f,
          isSold = false,
          co2SavedKg = 6.8
        ),
        ProductEntity(
          id = "prod_3",
          title = "Mid-Century Ribbed Ceramic Table Lamp with Linen Shade",
          description = "Warm earth-tone stoneware lamp with handcrafted ridges and natural textured linen drum shade. Cord re-wired to modern safety standards with inline switch. Warm ambient light.",
          price = 54.0,
          originalPrice = 110.0,
          category = "Home & Living",
          condition = "Good",
          imageUrl = "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=800&q=80",
          sellerId = "user_maya",
          sellerName = "Maya Lin",
          sellerRating = 4.88f,
          isSold = false,
          co2SavedKg = 3.1
        ),
        ProductEntity(
          id = "prod_4",
          title = "First Edition Hardcover 'The Art of Color' (1970)",
          description = "Rare vintage design monograph by Johannes Itten. Cloth bound with gilt typography on spine. Minor shelf wear on corners, interior pages crisp, vibrant Bauhaus color plates intact.",
          price = 42.0,
          originalPrice = 95.0,
          category = "Books",
          condition = "Good",
          imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=800&q=80",
          sellerId = "user_leo",
          sellerName = "Leo Sterling",
          sellerRating = 4.92f,
          isSold = false,
          co2SavedKg = 1.9
        ),
        ProductEntity(
          id = "prod_5",
          title = "Mechanical Seiko 5 Automatic Day-Date (Navy Dial)",
          description = "Timeless 37mm dress/everyday automatic timepiece. 7S26 movement serviced recently, keeps -4s/day. Mounted on a caramel Horween leather strap. Original steel bracelet included.",
          price = 120.0,
          originalPrice = 210.0,
          category = "Collectibles",
          condition = "Like New",
          imageUrl = "https://images.unsplash.com/photo-1524805444758-089113d48a6d?auto=format&fit=crop&w=800&q=80",
          sellerId = "user_leo",
          sellerName = "Leo Sterling",
          sellerRating = 4.92f,
          isSold = false,
          co2SavedKg = 5.0
        ),
        ProductEntity(
          id = "prod_6",
          title = "Handmade Wool Berber Throw Blanket (Terracotta/Oat)",
          description = "Heavy 100% natural virgin wool throw blanket washed and steam cleaned. Thick geometric pattern with braided fringe tassels. Cozy, breathable, and warm.",
          price = 78.0,
          originalPrice = 160.0,
          category = "Home & Living",
          condition = "Brand New",
          imageUrl = "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?auto=format&fit=crop&w=800&q=80",
          sellerId = "user_me",
          sellerName = "Alex Morgan",
          sellerRating = 4.95f,
          isSold = false,
          co2SavedKg = 3.8
        ),
        ProductEntity(
          id = "prod_7",
          title = "Sony WH-1000XM4 Wireless Noise-Cancelling Headphones",
          description = "Gently used for 4 months in home office. Battery health at 98%, earcups sanitized and spotless. Comes with original hardshell case, airplane adapter, and aux cable.",
          price = 165.0,
          originalPrice = 348.0,
          category = "Electronics",
          condition = "Like New",
          imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80",
          sellerId = "user_maya",
          sellerName = "Maya Lin",
          sellerRating = 4.88f,
          isSold = false,
          co2SavedKg = 7.4
        ),
        ProductEntity(
          id = "prod_8",
          title = "Vintage French Workwear Cotton Moleskin Pants",
          description = "Heavyweight brushed French cotton trousers in dark moss olive. Button fly, high-rise straight cut, waist 32 x length 30. Beautiful soft drape that lasts decades.",
          price = 85.0,
          originalPrice = 190.0,
          category = "Vintage",
          condition = "Good",
          imageUrl = "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?auto=format&fit=crop&w=800&q=80",
          sellerId = "user_me",
          sellerName = "Alex Morgan",
          sellerRating = 4.95f,
          isSold = false,
          co2SavedKg = 4.5
        )
      )

      productDao.insertAllProducts(seedProducts)
    }
  }
}
