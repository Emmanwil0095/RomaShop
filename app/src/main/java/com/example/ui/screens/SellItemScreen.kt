package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Categories
import com.example.data.model.Conditions
import com.example.ui.theme.PineGreen
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.MarketplaceViewModel

@Composable
fun SellItemScreen(
  viewModel: MarketplaceViewModel,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.navigateBack()
  }

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var priceText by remember { mutableStateOf("") }
  var originalPriceText by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Fashion") }
  var selectedCondition by remember { mutableStateOf("Like New") }
  var selectedImageUri by remember {
    mutableStateOf("https://images.unsplash.com/photo-1544441893-675973e31985?auto=format&fit=crop&w=800&q=80")
  }

  // Zero-permission Android Photo Picker for high security and Play Policy compliance
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
    onResult = { uri: Uri? ->
      if (uri != null) {
        selectedImageUri = uri.toString()
      }
    }
  )

  val samplePhotos = listOf(
    "Vintage Sweater" to "https://images.unsplash.com/photo-1576566588028-4147f3842f27?auto=format&fit=crop&w=800&q=80",
    "Leather Bag" to "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=800&q=80",
    "Retro Sunglasses" to "https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=800&q=80",
    "Ceramic Mug" to "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=800&q=80",
    "Analog Watch" to "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=800&q=80"
  )

  val scrollState = rememberScrollState()
  val categoriesList = Categories.ALL.filter { it != "All" }

  val priceDouble = priceText.toDoubleOrNull() ?: 0.0
  val originalPriceDouble = originalPriceText.toDoubleOrNull() ?: (priceDouble * 1.5)
  val canSubmit = title.isNotBlank() && description.isNotBlank() && priceDouble > 0

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 16.dp)
        .padding(bottom = 100.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "List an Item for Sale",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Give your pre-loved goods a second life on Relove marketplace.",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 1. Photo Section
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Item Photo",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(10.dp))

          // Preview image box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
          ) {
            AsyncImage(
              model = ImageRequest.Builder(LocalContext.current)
                .data(selectedImageUri)
                .crossfade(true)
                .placeholder(R.drawable.marketplace_hero)
                .error(R.drawable.marketplace_hero)
                .build(),
              contentDescription = "Listing Image Preview",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )

            Button(
              onClick = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              shape = RoundedCornerShape(20.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .testTag("upload_photo_button")
            ) {
              Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                tint = PineGreen,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Choose Gallery Photo", color = PineGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Quick preset library
          Text(
            text = "Or choose sample preset photo:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            samplePhotos.forEach { (name, url) ->
              val isSelected = selectedImageUri == url
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isSelected) PineGreen else Color.Transparent
                ),
                modifier = Modifier.clickable { selectedImageUri = url }
              ) {
                Text(
                  text = name,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) PineGreen else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Listing Details Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Listing Details",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Title
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Listing Title (e.g. Vintage 90s Leather Jacket)") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("sell_title_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Price & Original Price
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = priceText,
              onValueChange = { priceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
              label = { Text("Your Price ($)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("sell_price_input")
            )

            OutlinedTextField(
              value = originalPriceText,
              onValueChange = { originalPriceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
              label = { Text("Retail Price ($)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("sell_original_price_input")
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Category Chips
          Text(
            text = "Category",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            categoriesList.forEach { cat ->
              FilterChip(
                selected = selectedCategory == cat,
                onClick = { selectedCategory = cat },
                label = { Text(cat, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PineGreen,
                  selectedLabelColor = Color.White
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Condition Chips
          Text(
            text = "Condition",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Conditions.ALL.forEach { cond ->
              FilterChip(
                selected = selectedCondition == cond,
                onClick = { selectedCondition = cond },
                label = { Text(cond, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.secondary,
                  selectedLabelColor = Color.White
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Description
          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description & Provenance") },
            placeholder = { Text("Describe fit, material, age, condition flaws or history...") },
            minLines = 4,
            maxLines = 6,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("sell_description_input")
          )
        }
      }
    }

    // Sticky Publish Button
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
          if (canSubmit) {
            viewModel.createListing(
              title = title,
              description = description,
              price = priceDouble,
              originalPrice = originalPriceDouble,
              category = selectedCategory,
              condition = selectedCondition,
              imageUrl = selectedImageUri
            )
          }
        },
        enabled = canSubmit,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp)
          .height(52.dp)
          .testTag("publish_listing_button")
      ) {
        Icon(
          imageVector = Icons.Default.Sell,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (canSubmit) "Publish Listing for $${String.format("%.2f", priceDouble)}" else "Fill in Details to Publish",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = Color.White
        )
      }
    }
  }
}
