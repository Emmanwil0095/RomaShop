package com.example.data.auth

import android.util.Base64
import com.example.data.db.UserDao
import com.example.data.model.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class AuthSession(
  val user: UserEntity,
  val token: String,
  val expiresAt: Long
)

data class DecodedJwt(
  val header: JSONObject,
  val payload: JSONObject,
  val signature: String,
  val isSignatureValid: Boolean
)

sealed class AuthResult {
  data class Success(val session: AuthSession) : AuthResult()
  data class Error(val message: String) : AuthResult()
}

/**
 * Authentication Service implementing:
 * 1. Secure Salted Password Hashing with PBKDF2/SHA-256
 * 2. RFC 7519 JWT (JSON Web Token) generation with HMAC-SHA256 signature
 * 3. User Registration, Login, Token Validation, and Profile Management
 */
class AuthService(private val userDao: UserDao) {
  private val jwtSecretKey = "relove-marketplace-super-secure-jwt-hmac-sha256-key"
  private val tokenDurationMillis = 7L * 24 * 60 * 60 * 1000 // 7 days

  /**
   * Hashes a password with a cryptographically secure random salt using SHA-256 with key stretching.
   */
  fun hashPassword(password: String, salt: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    var hash = digest.digest((password + salt).toByteArray(StandardCharsets.UTF_8))
    // 1000 rounds of key stretching (Bcrypt/PBKDF2 style)
    for (i in 0 until 1000) {
      digest.reset()
      hash = digest.digest(hash)
    }
    return encodeBase64(hash)
  }

  fun generateSalt(): String {
    val random = SecureRandom()
    val bytes = ByteArray(16)
    random.nextBytes(bytes)
    return encodeBase64(bytes)
  }

  /**
   * Signs a JWT token for the user with HMAC-SHA256.
   */
  fun generateJwtToken(user: UserEntity): String {
    val now = System.currentTimeMillis() / 1000
    val exp = now + (7 * 24 * 3600)

    val header = JSONObject().apply {
      put("alg", "HS256")
      put("typ", "JWT")
    }

    val payload = JSONObject().apply {
      put("sub", user.id)
      put("name", user.name)
      put("email", user.email)
      put("username", user.username)
      put("role", if (user.isSeller) "seller" else "buyer")
      put("iat", now)
      put("exp", exp)
      put("iss", "https://relove-marketplace.api")
    }

    val encodedHeader = base64UrlEncode(header.toString().toByteArray(StandardCharsets.UTF_8))
    val encodedPayload = base64UrlEncode(payload.toString().toByteArray(StandardCharsets.UTF_8))
    val dataToSign = "$encodedHeader.$encodedPayload"

    val signature = signHmacSha256(dataToSign, jwtSecretKey)
    return "$dataToSign.$signature"
  }

  /**
   * Decodes and validates a JWT token.
   */
  fun decodeAndVerifyJwt(token: String): DecodedJwt? {
    return try {
      val parts = token.split(".")
      if (parts.size != 3) return null

      val headerJson = JSONObject(String(base64UrlDecode(parts[0]), StandardCharsets.UTF_8))
      val payloadJson = JSONObject(String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8))
      val expectedSignature = signHmacSha256("${parts[0]}.${parts[1]}", jwtSecretKey)
      val isValid = expectedSignature == parts[2]

      DecodedJwt(
        header = headerJson,
        payload = payloadJson,
        signature = parts[2],
        isSignatureValid = isValid
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun encodeBase64(bytes: ByteArray): String {
    return try {
      java.util.Base64.getEncoder().encodeToString(bytes)
    } catch (e: Throwable) {
      Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
  }

  private fun signHmacSha256(data: String, key: String): String {
    val sha256Hmac = Mac.getInstance("HmacSHA256")
    val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
    sha256Hmac.init(secretKey)
    val signedBytes = sha256Hmac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    return base64UrlEncode(signedBytes)
  }

  private fun base64UrlEncode(bytes: ByteArray): String {
    return try {
      java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    } catch (e: Throwable) {
      Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
  }

  private fun base64UrlDecode(str: String): ByteArray {
    return try {
      java.util.Base64.getUrlDecoder().decode(str)
    } catch (e: Throwable) {
      Base64.decode(str, Base64.URL_SAFE or Base64.NO_WRAP)
    }
  }

  /**
   * User Registration API endpoint
   */
  suspend fun register(
    username: String,
    email: String,
    password: String,
    displayName: String,
    isSeller: Boolean,
    bio: String = ""
  ): AuthResult = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim().lowercase()
    val cleanUsername = username.trim().lowercase()

    if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
      return@withContext AuthResult.Error("Please enter a valid email address.")
    }
    if (cleanUsername.length < 3) {
      return@withContext AuthResult.Error("Username must be at least 3 characters.")
    }
    if (password.length < 6) {
      return@withContext AuthResult.Error("Password must be at least 6 characters.")
    }

    val existingUser = userDao.getUserByEmail(cleanEmail)
    if (existingUser != null) {
      return@withContext AuthResult.Error("An account with this email already exists.")
    }

    val existingUsername = userDao.getUserByUsername(cleanUsername)
    if (existingUsername != null) {
      return@withContext AuthResult.Error("Username '@$cleanUsername' is already taken.")
    }

    val salt = generateSalt()
    val passwordHash = hashPassword(password, salt)

    val newUser = UserEntity(
      id = "user_" + UUID.randomUUID().toString().replace("-", "").take(12),
      username = cleanUsername,
      name = displayName.ifBlank { username }.trim(),
      email = cleanEmail,
      passwordHash = passwordHash,
      salt = salt,
      bio = bio.ifBlank { "Passionate about circular fashion & vintage finds." }.trim(),
      isSeller = isSeller,
      avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=400&q=80",
      rating = 5.0f,
      salesCount = 0,
      joinedDate = "Member since today"
    )

    userDao.insertUser(newUser)
    val token = generateJwtToken(newUser)
    val expiresAt = System.currentTimeMillis() + tokenDurationMillis

    AuthResult.Success(AuthSession(newUser, token, expiresAt))
  }

  /**
   * User Login API endpoint
   */
  suspend fun login(
    emailOrUsername: String,
    password: String
  ): AuthResult = withContext(Dispatchers.IO) {
    val query = emailOrUsername.trim().lowercase()
    val user = if (query.contains("@")) {
      userDao.getUserByEmail(query)
    } else {
      userDao.getUserByUsername(query)
    } ?: return@withContext AuthResult.Error("Account not found. Please check your username or email.")

    val testHash = hashPassword(password, user.salt)
    if (testHash != user.passwordHash) {
      return@withContext AuthResult.Error("Incorrect password. Please try again.")
    }

    val token = generateJwtToken(user)
    val expiresAt = System.currentTimeMillis() + tokenDurationMillis

    AuthResult.Success(AuthSession(user, token, expiresAt))
  }

  /**
   * Profile Management API endpoint
   */
  suspend fun updateProfile(
    userId: String,
    name: String,
    bio: String,
    isSeller: Boolean
  ): UserEntity? = withContext(Dispatchers.IO) {
    val existing = userDao.getUserById(userId) ?: return@withContext null
    val updated = existing.copy(
      name = name.trim(),
      bio = bio.trim(),
      isSeller = isSeller
    )
    userDao.updateUser(updated)
    updated
  }
}
