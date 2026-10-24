package com.example.data.payment

import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Stripe-compatible Payment Intent and Gateway structures.
 * Strictly adheres to PCI-DSS practices: full card numbers and CVV codes
 * are processed strictly in-memory during tokenization and NEVER stored in the database.
 */
data class PaymentIntent(
  val id: String,
  val clientSecret: String,
  val amountCents: Long,
  val currency: String = "usd",
  val status: PaymentIntentStatus = PaymentIntentStatus.REQUIRES_PAYMENT_METHOD,
  val createdAt: Long = System.currentTimeMillis()
)

enum class PaymentIntentStatus {
  REQUIRES_PAYMENT_METHOD,
  PROCESSING,
  SUCCEEDED,
  FAILED,
  CANCELED
}

sealed class PaymentResult {
  data class Success(
    val paymentIntentId: String,
    val transactionId: String,
    val cardBrand: String,
    val last4: String,
    val receiptUrl: String
  ) : PaymentResult()

  data class Failure(
    val errorCode: String,
    val errorMessage: String,
    val declineCode: String? = null
  ) : PaymentResult()

  object Canceled : PaymentResult()
}

data class CardDetails(
  val cardNumber: String,
  val expiryDate: String, // MM/YY
  val cvc: String,
  val cardholderName: String
)

enum class CardBrand(val displayName: String, val icon: String) {
  VISA("Visa", "💳 Visa"),
  MASTERCARD("Mastercard", "💳 Mastercard"),
  AMEX("American Express", "💳 AMEX"),
  DISCOVER("Discover", "💳 Discover"),
  UNKNOWN("Card", "💳 Card")
}

class StripePaymentGateway {

  /**
   * Creates a Stripe PaymentIntent for the specified order total.
   */
  suspend fun createPaymentIntent(amountDollars: Double, currency: String = "usd"): PaymentIntent {
    delay(350) // simulate gateway handshake
    val amountCents = (amountDollars * 100).toLong()
    val id = "pi_" + UUID.randomUUID().toString().replace("-", "").take(24)
    val clientSecret = "${id}_secret_" + UUID.randomUUID().toString().take(16)

    return PaymentIntent(
      id = id,
      clientSecret = clientSecret,
      amountCents = amountCents,
      currency = currency,
      status = PaymentIntentStatus.REQUIRES_PAYMENT_METHOD
    )
  }

  /**
   * Confirms payment for a given PaymentIntent using card details.
   * Validates card format, Luhn algorithm, handles Stripe standard test cards,
   * and yields secure callback results.
   */
  suspend fun confirmPayment(
    paymentIntentId: String,
    card: CardDetails
  ): PaymentResult {
    // 1. In-memory validation
    val cleanCardNumber = card.cardNumber.replace("\\s".toRegex(), "").replace("-", "")
    val cleanCvc = card.cvc.trim()
    val cleanExpiry = card.expiryDate.trim()

    if (cleanCardNumber.length < 13 || cleanCardNumber.length > 19) {
      return PaymentResult.Failure(
        errorCode = "invalid_number",
        errorMessage = "The card number is invalid or has incorrect digits."
      )
    }

    if (!isValidLuhn(cleanCardNumber)) {
      return PaymentResult.Failure(
        errorCode = "invalid_number",
        errorMessage = "The card number failed the checksum verification."
      )
    }

    if (!isValidExpiry(cleanExpiry)) {
      return PaymentResult.Failure(
        errorCode = "invalid_expiry",
        errorMessage = "The card's expiration date is invalid (use MM/YY)."
      )
    }

    if (cleanCvc.length !in 3..4) {
      return PaymentResult.Failure(
        errorCode = "invalid_cvc",
        errorMessage = "The security code (CVC) must be 3 or 4 digits."
      )
    }

    if (card.cardholderName.trim().isBlank()) {
      return PaymentResult.Failure(
        errorCode = "missing_name",
        errorMessage = "Please enter the cardholder's full name."
      )
    }

    // 2. Gateway Network Processing Simulation (tokenization & 3DS authorization)
    delay(1200)

    // 3. Handle standard Stripe Test Card responses
    when {
      cleanCardNumber.endsWith("0002") -> {
        return PaymentResult.Failure(
          errorCode = "card_declined",
          errorMessage = "Your card was declined by the issuing bank.",
          declineCode = "generic_decline"
        )
      }
      cleanCardNumber.endsWith("0127") -> {
        return PaymentResult.Failure(
          errorCode = "card_declined",
          errorMessage = "Insufficient funds in the card account to complete this purchase.",
          declineCode = "insufficient_funds"
        )
      }
      cleanCardNumber.endsWith("0069") -> {
        return PaymentResult.Failure(
          errorCode = "expired_card",
          errorMessage = "Your card has expired. Please use a different card.",
          declineCode = "expired_card"
        )
      }
      cleanCardNumber.endsWith("9995") -> {
        return PaymentResult.Failure(
          errorCode = "processing_error",
          errorMessage = "A temporary network error occurred while communicating with Stripe. Please try again.",
          declineCode = "processing_error"
        )
      }
      else -> {
        val last4 = cleanCardNumber.takeLast(4)
        val brand = detectCardBrand(cleanCardNumber).displayName
        val txnId = "txn_" + UUID.randomUUID().toString().replace("-", "").take(20)
        val receipt = "https://pay.stripe.com/receipts/relove_$paymentIntentId"

        return PaymentResult.Success(
          paymentIntentId = paymentIntentId,
          transactionId = txnId,
          cardBrand = brand,
          last4 = last4,
          receiptUrl = receipt
        )
      }
    }
  }

  fun detectCardBrand(cardNumber: String): CardBrand {
    val clean = cardNumber.replace("\\s".toRegex(), "")
    return when {
      clean.startsWith("4") -> CardBrand.VISA
      clean.startsWith("51") || clean.startsWith("52") || clean.startsWith("53") ||
        clean.startsWith("54") || clean.startsWith("55") || clean.startsWith("22") -> CardBrand.MASTERCARD
      clean.startsWith("34") || clean.startsWith("37") -> CardBrand.AMEX
      clean.startsWith("6011") || clean.startsWith("65") -> CardBrand.DISCOVER
      else -> CardBrand.UNKNOWN
    }
  }

  private fun isValidLuhn(number: String): Boolean {
    var sum = 0
    var alternate = false
    for (i in number.length - 1 downTo 0) {
      var n = number[i].digitToIntOrNull() ?: return false
      if (alternate) {
        n *= 2
        if (n > 9) n = (n % 10) + 1
      }
      sum += n
      alternate = !alternate
    }
    return (sum % 10 == 0)
  }

  private fun isValidExpiry(expiry: String): Boolean {
    val parts = expiry.split("/")
    if (parts.size != 2) return false
    val month = parts[0].trim().toIntOrNull() ?: return false
    val year = parts[1].trim().toIntOrNull() ?: return false
    if (month !in 1..12) return false
    val fullYear = if (year < 100) 2000 + year else year
    return fullYear >= 2024
  }
}
