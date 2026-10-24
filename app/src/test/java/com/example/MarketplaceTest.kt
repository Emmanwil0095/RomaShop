package com.example

import com.example.data.auth.AuthService
import com.example.data.model.UserEntity
import com.example.data.payment.CardDetails
import com.example.data.payment.PaymentResult
import com.example.data.payment.StripePaymentGateway
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MarketplaceTest {

  @Test
  fun testPasswordHashingAndSalting() {
    val authService = AuthService(FakeUserDao())
    val password = "SuperSecretPassword123!"
    val salt1 = authService.generateSalt()
    val salt2 = authService.generateSalt()

    val hash1 = authService.hashPassword(password, salt1)
    val hash1Repeat = authService.hashPassword(password, salt1)
    val hash2 = authService.hashPassword(password, salt2)

    // Salt ensures hashes are non-deterministic across different users
    assertNotEquals(hash1, hash2)
    // Repeat with identical salt produces identical hash
    assertEquals(hash1, hash1Repeat)
    // Raw password never matches hash
    assertNotEquals(password, hash1)
  }

  @Test
  fun testJwtTokenGenerationAndVerification() {
    val authService = AuthService(FakeUserDao())
    val user = UserEntity(
      id = "user_test_99",
      username = "vintage_buyer",
      name = "Sam Taylor",
      email = "sam@vintage.eco",
      isSeller = true
    )

    val token = authService.generateJwtToken(user)
    assertNotNull(token)
    assertTrue(token.contains("."))
    val parts = token.split(".")
    assertEquals(3, parts.size)

    val decoded = authService.decodeAndVerifyJwt(token)
    assertNotNull(decoded)
    assertTrue(decoded!!.isSignatureValid)
    assertEquals("user_test_99", decoded.payload.getString("sub"))
    assertEquals("vintage_buyer", decoded.payload.getString("username"))
    assertEquals("sam@vintage.eco", decoded.payload.getString("email"))
    assertEquals("seller", decoded.payload.getString("role"))
  }

  @Test
  fun testStripePaymentIntentAndSuccessfulCharge() = runBlocking {
    val gateway = StripePaymentGateway()
    val intent = gateway.createPaymentIntent(89.50)

    assertTrue(intent.id.startsWith("pi_"))
    assertEquals(8950L, intent.amountCents)

    val validCard = CardDetails(
      cardNumber = "4242 4242 4242 4242",
      expiryDate = "12/28",
      cvc = "123",
      cardholderName = "Jane Doe"
    )

    val result = gateway.confirmPayment(intent.id, validCard)
    assertTrue(result is PaymentResult.Success)
    val success = result as PaymentResult.Success
    assertEquals("Visa", success.cardBrand)
    assertEquals("4242", success.last4)
    assertEquals(intent.id, success.paymentIntentId)
  }

  @Test
  fun testStripePaymentDeclineSimulation() = runBlocking {
    val gateway = StripePaymentGateway()
    val intent = gateway.createPaymentIntent(150.0)

    val declineCard = CardDetails(
      cardNumber = "4000 0000 0000 0002",
      expiryDate = "10/27",
      cvc = "999",
      cardholderName = "Alex Tester"
    )

    val result = gateway.confirmPayment(intent.id, declineCard)
    assertTrue(result is PaymentResult.Failure)
    val failure = result as PaymentResult.Failure
    assertEquals("card_declined", failure.errorCode)
    assertEquals("generic_decline", failure.declineCode)
  }

  @Test
  fun testStripeInsufficientFundsSimulation() = runBlocking {
    val gateway = StripePaymentGateway()
    val intent = gateway.createPaymentIntent(299.0)

    val lowFundsCard = CardDetails(
      cardNumber = "4000 0000 0000 0127",
      expiryDate = "08/29",
      cvc = "456",
      cardholderName = "Alex Tester"
    )

    val result = gateway.confirmPayment(intent.id, lowFundsCard)
    assertTrue(result is PaymentResult.Failure)
    val failure = result as PaymentResult.Failure
    assertEquals("card_declined", failure.errorCode)
    assertEquals("insufficient_funds", failure.declineCode)
  }
}

// Minimal mock DAO for testing auth logic independently of database
private class FakeUserDao : com.example.data.db.UserDao {
  private val list = mutableListOf<UserEntity>()
  override fun getAllUsers() = kotlinx.coroutines.flow.flowOf(list)
  override suspend fun getUserById(id: String) = list.find { it.id == id }
  override suspend fun getUserByEmail(email: String) = list.find { it.email == email }
  override suspend fun getUserByUsername(username: String) = list.find { it.username == username }
  override suspend fun insertUser(user: UserEntity) { list.add(user) }
  override suspend fun insertAllUsers(users: List<UserEntity>) { list.addAll(users) }
  override suspend fun updateUser(user: UserEntity) {}
}
