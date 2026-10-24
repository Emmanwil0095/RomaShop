package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.payment.CardDetails
import com.example.ui.theme.PineGreen
import com.example.ui.theme.SageGreen
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.MarketplaceViewModel
import com.example.ui.viewmodel.PaymentUiState

@Composable
fun CheckoutScreen(
  viewModel: MarketplaceViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.resetPaymentState()
    viewModel.navigateBack()
  }

  val user by viewModel.currentUser.collectAsState()
  val cartItems by viewModel.cartItems.collectAsState()
  val subtotal by viewModel.cartSubtotal.collectAsState()
  val paymentUiState by viewModel.paymentUiState.collectAsState()

  val shippingFee = if (subtotal >= 50.0 || subtotal == 0.0) 0.0 else 4.99
  val total = subtotal + shippingFee

  var fullName by remember { mutableStateOf(user.name) }
  var email by remember { mutableStateOf(user.email) }
  var address by remember { mutableStateOf("742 Evergreen Terrace") }
  var cityZip by remember { mutableStateOf("Springfield, OR 97477") }
  var selectedPaymentMethod by remember { mutableStateOf("Stripe") }

  // Stripe Card Details (handled strictly in-memory)
  var cardNumber by remember { mutableStateOf("4242 4242 4242 4242") }
  var cardExpiry by remember { mutableStateOf("12/28") }
  var cardCvc by remember { mutableStateOf("123") }
  var cardholderName by remember { mutableStateOf(user.name) }

  val detectedBrand = remember(cardNumber) {
    viewModel.paymentGateway.detectCardBrand(cardNumber)
  }

  val scrollState = rememberScrollState()

  // Payment Error Modal
  if (paymentUiState is PaymentUiState.Error) {
    val errorState = paymentUiState as PaymentUiState.Error
    AlertDialog(
      onDismissRequest = { viewModel.resetPaymentState() },
      icon = {
        Icon(
          imageVector = Icons.Default.ErrorOutline,
          contentDescription = null,
          tint = Terracotta,
          modifier = Modifier.size(36.dp)
        )
      },
      title = {
        Text(
          text = "Payment Gateway Notice",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      },
      text = {
        Column {
          Text(
            text = errorState.message,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (errorState.declineCode != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = "Stripe Error Code: ${errorState.declineCode}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { viewModel.resetPaymentState() },
          colors = ButtonDefaults.buttonColors(containerColor = PineGreen)
        ) {
          Text("Try Another Card / Retry")
        }
      }
    )
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 16.dp)
        .padding(bottom = 120.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Header Steps
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        CheckoutStep(number = "1", title = "Shipping", active = true)
        HorizontalDivider(modifier = Modifier.weight(0.3f), color = PineGreen)
        CheckoutStep(number = "2", title = "Stripe Pay", active = true)
        HorizontalDivider(modifier = Modifier.weight(0.3f), color = PineGreen)
        CheckoutStep(number = "3", title = "Confirm", active = true)
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 1. Shipping Details Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocalShipping,
              contentDescription = null,
              tint = PineGreen,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "1. Delivery Information",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("checkout_name_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Receipt Email") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("checkout_email_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Street Address") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("checkout_address_input")
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = cityZip,
            onValueChange = { cityZip = it },
            label = { Text("City, State & ZIP Code") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("checkout_city_zip_input")
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Stripe Payment Gateway Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Payment,
                contentDescription = null,
                tint = PineGreen,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "2. Payment Method",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = "STRIPE SECURED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PineGreen,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Stripe Option Radio
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (selectedPaymentMethod == "Stripe") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(
              width = if (selectedPaymentMethod == "Stripe") 1.5.dp else 1.dp,
              color = if (selectedPaymentMethod == "Stripe") PineGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedPaymentMethod = "Stripe" }
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = selectedPaymentMethod == "Stripe",
                onClick = { selectedPaymentMethod = "Stripe" },
                colors = RadioButtonDefaults.colors(selectedColor = PineGreen)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = PineGreen)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Stripe Payment Intent (Credit / Debit Card)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Instant tokenization & direct bank settlement", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }

          // Stripe Interactive Card Fields (shown when Stripe selected)
          if (selectedPaymentMethod == "Stripe") {
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                // Card brand header
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "CARD DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = PineGreen
                  ) {
                    Text(
                      text = detectedBrand.displayName,
                      color = Color.White,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Card Number field
                OutlinedTextField(
                  value = cardNumber,
                  onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(16)
                    cardNumber = digits.chunked(4).joinToString(" ")
                  },
                  label = { Text("Card Number") },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.CreditCard,
                      contentDescription = null,
                      tint = PineGreen
                    )
                  },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  singleLine = true,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stripe_card_number")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Expiry and CVC Row
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  OutlinedTextField(
                    value = cardExpiry,
                    onValueChange = { input ->
                      val digits = input.filter { it.isDigit() }.take(4)
                      cardExpiry = if (digits.length >= 3) {
                        "${digits.take(2)}/${digits.drop(2)}"
                      } else {
                        digits
                      }
                    },
                    label = { Text("Expiry (MM/YY)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                      .weight(1f)
                      .testTag("stripe_card_expiry")
                  )

                  OutlinedTextField(
                    value = cardCvc,
                    onValueChange = { input ->
                      cardCvc = input.filter { it.isDigit() }.take(4)
                    },
                    label = { Text("CVC") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                      .weight(1f)
                      .testTag("stripe_card_cvc")
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                  value = cardholderName,
                  onValueChange = { cardholderName = it },
                  label = { Text("Name on Card") },
                  singleLine = true,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stripe_cardholder_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Test Card Presets for easy QA
                Text(
                  text = "Stripe Test Cards:",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                      .clickable {
                        cardNumber = "4242 4242 4242 4242"
                        cardExpiry = "12/28"
                        cardCvc = "123"
                      }
                  ) {
                    Text(
                      text = "✓ Success (4242)",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = PineGreen,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }

                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Terracotta.copy(alpha = 0.15f),
                    modifier = Modifier
                      .clickable {
                        cardNumber = "4000 0000 0000 0002"
                        cardExpiry = "12/28"
                        cardCvc = "123"
                      }
                  ) {
                    Text(
                      text = "✗ Decline (0002)",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = Terracotta,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }

                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                      .clickable {
                        cardNumber = "4000 0000 0000 0127"
                        cardExpiry = "12/28"
                        cardCvc = "123"
                      }
                  ) {
                    Text(
                      text = "✗ Low Funds (0127)",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Security Guarantee
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = PineGreen,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Zero raw card numbers stored in DB. Secure tokenization only.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Alternative Option: Cash on Delivery / In-person pickup
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (selectedPaymentMethod == "Cash on Delivery") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(
              width = if (selectedPaymentMethod == "Cash on Delivery") 1.5.dp else 1.dp,
              color = if (selectedPaymentMethod == "Cash on Delivery") PineGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedPaymentMethod = "Cash on Delivery" }
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = selectedPaymentMethod == "Cash on Delivery",
                onClick = { selectedPaymentMethod = "Cash on Delivery" },
                colors = RadioButtonDefaults.colors(selectedColor = PineGreen)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Cash on Delivery / Pickup", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Pay seller directly upon local handover", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3. Order Breakdown Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "3. Order Total",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(8.dp))

          cartItems.forEach { item ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "${item.cartItem.quantity}x ${item.product.title}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1
              )
              Text(
                text = "$${String.format("%.2f", item.product.price * item.cartItem.quantity)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Subtotal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$${String.format("%.2f", subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Carbon-Neutral Shipping", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              if (shippingFee == 0.0) "FREE" else "$${String.format("%.2f", shippingFee)}",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (shippingFee == 0.0) PineGreen else MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Total Amount Due", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
              "$${String.format("%.2f", total)}",
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = PineGreen
            )
          }
        }
      }
    }

    // Sticky Bottom Confirmation Button
    val isBusy = paymentUiState is PaymentUiState.CreatingPaymentIntent || paymentUiState is PaymentUiState.ProcessingPayment

    Surface(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .navigationBarsPadding(),
      shadowElevation = 12.dp,
      color = MaterialTheme.colorScheme.surface
    ) {
      Button(
        onClick = {
          if (!isBusy && fullName.isNotBlank() && address.isNotBlank()) {
            if (selectedPaymentMethod == "Stripe") {
              viewModel.processStripePayment(
                buyerName = fullName,
                buyerEmail = email,
                shippingAddress = "$address, $cityZip",
                card = CardDetails(
                  cardNumber = cardNumber,
                  expiryDate = cardExpiry,
                  cvc = cardCvc,
                  cardholderName = cardholderName
                )
              )
            } else {
              viewModel.checkout(
                buyerName = fullName,
                buyerEmail = email,
                shippingAddress = "$address, $cityZip",
                paymentMethod = selectedPaymentMethod
              )
            }
          }
        },
        enabled = !isBusy && fullName.isNotBlank() && address.isNotBlank(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp)
          .height(52.dp)
          .testTag("confirm_order_button")
      ) {
        if (isBusy) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
              color = Color.White,
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (paymentUiState is PaymentUiState.CreatingPaymentIntent) "Creating Payment Intent..." else "Authorizing with Stripe...",
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        } else {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (selectedPaymentMethod == "Stripe") "Pay $${String.format("%.2f", total)} via Stripe" else "Place Order ($${String.format("%.2f", total)})",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White
          )
        }
      }
    }
  }
}

@Composable
private fun CheckoutStep(number: String, title: String, active: Boolean) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(if (active) PineGreen else MaterialTheme.colorScheme.outline),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = number,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
    }
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = title,
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      color = if (active) PineGreen else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
