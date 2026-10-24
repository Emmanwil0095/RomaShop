package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.PineGreen
import com.example.ui.theme.SageGreen
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.MarketplaceViewModel

@Composable
fun ProfileScreen(
  viewModel: MarketplaceViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val jwtToken by viewModel.jwtToken.collectAsState()
  val allUsers by viewModel.allUsers.collectAsState()

  var showEditProfileDialog by remember { mutableStateOf(false) }
  var showAuthDialog by remember { mutableStateOf(false) }
  var showSwitchAccountDialog by remember { mutableStateOf(false) }
  var showTokenClaimsDialog by remember { mutableStateOf(false) }

  val decodedJwt = remember(jwtToken) {
    viewModel.repository.authService.decodeAndVerifyJwt(jwtToken)
  }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp)
      .padding(bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. User Header Card
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(PineGreen),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = currentUser.name.take(2).uppercase(),
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = currentUser.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (currentUser.isSeller) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
              ) {
                Text(
                  text = if (currentUser.isSeller) "SELLER" else "BUYER",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (currentUser.isSeller) PineGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Text(
              text = "@${currentUser.username} • ${currentUser.email}",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
              text = currentUser.joinedDate,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
          }

          IconButton(
            onClick = { showEditProfileDialog = true },
            modifier = Modifier.testTag("edit_profile_button")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Profile",
              tint = PineGreen
            )
          }
        }

        if (currentUser.bio.isNotBlank()) {
          Spacer(modifier = Modifier.height(14.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = currentUser.bio,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(12.dp),
              lineHeight = 18.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Quick Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = { showSwitchAccountDialog = true },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(imageVector = Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Switch User", fontSize = 12.sp)
          }

          Button(
            onClick = { showAuthDialog = true },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.weight(1f)
          ) {
            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Sign In / Join", fontSize = 12.sp)
          }
        }
      }
    }

    // 2. JWT Authentication & Security Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Key,
              contentDescription = null,
              tint = PineGreen,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "JWT Security & Identity",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = PineGreen, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("HMAC-SHA256 SIGNED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PineGreen)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Active RFC 7519 JWT Bearer Token:",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showTokenClaimsDialog = true }
        ) {
          Text(
            text = jwtToken.take(45) + "..." + jwtToken.takeLast(15),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(10.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Password Security: Salted SHA-256 + 1000 rounds",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          TextButton(onClick = { showTokenClaimsDialog = true }) {
            Text("Inspect Claims", fontSize = 12.sp, color = PineGreen, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 3. Circular Sustainability Impact
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            tint = PineGreen,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Your Eco Impact",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          ImpactStat(emoji = "🌿", value = "48.6 kg", label = "CO2 Prevented")
          ImpactStat(emoji = "💧", value = "18,400 L", label = "Water Saved")
          ImpactStat(emoji = "📦", value = "12 items", label = "Kept in Use")
        }
      }
    }
  }

  // --- MODALS & DIALOGS ---

  // 1. Edit Profile Dialog
  if (showEditProfileDialog) {
    var editName by remember { mutableStateOf(currentUser.name) }
    var editBio by remember { mutableStateOf(currentUser.bio) }
    var editIsSeller by remember { mutableStateOf(currentUser.isSeller) }

    AlertDialog(
      onDismissRequest = { showEditProfileDialog = false },
      title = { Text("Edit Profile & Role", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = editName,
            onValueChange = { editName = it },
            label = { Text("Display Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = editBio,
            onValueChange = { editBio = it },
            label = { Text("Bio / Curator Statement") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Seller Account Mode", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
              Text("List and sell pre-loved goods", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = editIsSeller,
              onCheckedChange = { editIsSeller = it },
              colors = SwitchDefaults.colors(checkedThumbColor = PineGreen, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateProfile(editName, editBio, editIsSeller)
            showEditProfileDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = PineGreen)
        ) {
          Text("Save Changes")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditProfileDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // 2. JWT Claims Inspector Dialog
  if (showTokenClaimsDialog && decodedJwt != null) {
    AlertDialog(
      onDismissRequest = { showTokenClaimsDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = PineGreen)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Decoded JWT Claims", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          Text(
            text = "Header (Algorithm & Type):",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = PineGreen
          )
          Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
              text = decodedJwt.header.toString(2),
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              modifier = Modifier.padding(8.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Payload (User Claims & Scopes):",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = PineGreen
          )
          Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
              text = decodedJwt.payload.toString(2),
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              modifier = Modifier.padding(8.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Signature Status: Verified ✓",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = PineGreen
          )
        }
      },
      confirmButton = {
        Button(onClick = { showTokenClaimsDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = PineGreen)) {
          Text("Close")
        }
      }
    )
  }

  // 3. Switch Account Dialog
  if (showSwitchAccountDialog) {
    AlertDialog(
      onDismissRequest = { showSwitchAccountDialog = false },
      title = { Text("Switch Demo Profile", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Select an existing profile in the database:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          allUsers.forEach { user ->
            val isCurrent = user.id == currentUser.id
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.switchUser(user)
                  showSwitchAccountDialog = false
                }
            ) {
              Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (user.isSeller) "🛍️" else "👤", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Text(text = "@${user.username} • ${if (user.isSeller) "Seller" else "Buyer"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showSwitchAccountDialog = false }) { Text("Close") }
      }
    )
  }

  // 4. Sign In / Register Auth Dialog with JWT
  if (showAuthDialog) {
    AuthModal(
      viewModel = viewModel,
      onDismiss = { showAuthDialog = false }
    )
  }
}

@Composable
private fun AuthModal(
  viewModel: MarketplaceViewModel,
  onDismiss: () -> Unit
) {
  var authTab by remember { mutableIntStateOf(0) } // 0 = Login, 1 = Register

  var loginIdentifier by remember { mutableStateOf("") }
  var loginPassword by remember { mutableStateOf("") }

  var regUsername by remember { mutableStateOf("") }
  var regEmail by remember { mutableStateOf("") }
  var regName by remember { mutableStateOf("") }
  var regPassword by remember { mutableStateOf("") }
  var regIsSeller by remember { mutableStateOf(true) }

  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      TabRow(selectedTabIndex = authTab, containerColor = Color.Transparent) {
        Tab(
          selected = authTab == 0,
          onClick = { authTab = 0; errorMessage = null },
          text = { Text("Sign In", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = authTab == 1,
          onClick = { authTab = 1; errorMessage = null },
          text = { Text("Create Account", fontWeight = FontWeight.Bold) }
        )
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
      ) {
        if (errorMessage != null) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Terracotta.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = errorMessage!!,
              color = Terracotta,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(8.dp)
            )
          }
        }

        if (authTab == 0) {
          // LOGIN
          Text("Sign in to your Relove account to view orders and manage listings.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

          OutlinedTextField(
            value = loginIdentifier,
            onValueChange = { loginIdentifier = it },
            label = { Text("Email or Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = loginPassword,
            onValueChange = { loginPassword = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
          )

          Text(
            text = "Demo hint: username 'alexmorgan' (pw: 'password123')",
            fontSize = 11.sp,
            color = PineGreen
          )
        } else {
          // REGISTER
          Text("Join Relove with secure password hashing and JWT token auth.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

          OutlinedTextField(
            value = regName,
            onValueChange = { regName = it },
            label = { Text("Full Name (e.g. Jordan Lee)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = regUsername,
            onValueChange = { regUsername = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' } },
            label = { Text("Username (handle)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = regEmail,
            onValueChange = { regEmail = it },
            label = { Text("Email Address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = regPassword,
            onValueChange = { regPassword = it },
            label = { Text("Password (min 6 chars)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Register as Seller", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Switch(
              checked = regIsSeller,
              onCheckedChange = { regIsSeller = it },
              colors = SwitchDefaults.colors(checkedThumbColor = PineGreen)
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (authTab == 0) {
            viewModel.login(loginIdentifier, loginPassword) { success, msg ->
              if (success) onDismiss() else errorMessage = msg
            }
          } else {
            viewModel.register(
              username = regUsername,
              email = regEmail,
              pass = regPassword,
              name = regName,
              isSeller = regIsSeller,
              bio = "Eco-conscious secondhand collector & seller."
            ) { success, msg ->
              if (success) onDismiss() else errorMessage = msg
            }
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = PineGreen)
      ) {
        Text(if (authTab == 0) "Sign In" else "Create Account")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
private fun ImpactStat(emoji: String, value: String, label: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = emoji, fontSize = 24.sp)
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
    Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}
