package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ProductEntity
import com.example.ui.components.CategoryChips
import com.example.ui.components.ProductCard
import com.example.ui.theme.PineGreen
import com.example.ui.theme.PineGreenDark
import com.example.ui.theme.SageGreen
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.MarketplaceViewModel
import com.example.ui.viewmodel.SortOption

@Composable
fun ExploreScreen(
  viewModel: MarketplaceViewModel,
  modifier: Modifier = Modifier
) {
  val products by viewModel.products.collectAsState()
  val selectedCategory by viewModel.selectedCategory.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedSort by viewModel.selectedSort.collectAsState()
  val favoriteIds by viewModel.favoriteIds.collectAsState()

  var showSortMenu by remember { mutableStateOf(false) }

  LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    modifier = modifier
      .fillMaxSize()
      .testTag("explore_grid"),
    contentPadding = PaddingValues(bottom = 24.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // 1. Hero Banner
    item(span = { GridItemSpan(2) }) {
      HeroBannerCard()
    }

    // 2. Search & Sort Bar
    item(span = { GridItemSpan(2) }) {
      Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search vintage, tech, fashion...", fontSize = 14.sp) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surface,
              unfocusedContainerColor = MaterialTheme.colorScheme.surface,
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("search_text_field")
          )

          Spacer(modifier = Modifier.width(8.dp))

          // Sort Button with dropdown
          Box {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surface,
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
              ),
              modifier = Modifier
                .height(56.dp)
                .clickable { showSortMenu = true }
                .testTag("sort_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.SwapVert,
                  contentDescription = "Sort Options",
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            }

            DropdownMenu(
              expanded = showSortMenu,
              onDismissRequest = { showSortMenu = false }
            ) {
              SortOption.values().forEach { option ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = option.label,
                      fontWeight = if (option == selectedSort) FontWeight.Bold else FontWeight.Normal,
                      color = if (option == selectedSort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                  },
                  onClick = {
                    viewModel.selectedSort.value = option
                    showSortMenu = false
                  }
                )
              }
            }
          }
        }
      }
    }

    // 3. Category Horizontal Chips
    item(span = { GridItemSpan(2) }) {
      CategoryChips(
        selectedCategory = selectedCategory,
        onSelectCategory = { viewModel.selectedCategory.value = it },
        modifier = Modifier.padding(vertical = 4.dp)
      )
    }

    // 4. Section Header with count and current sort label
    item(span = { GridItemSpan(2) }) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
      ) {
        Text(
          text = if (selectedCategory == "All") "Curated Secondhand" else selectedCategory,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${products.size} items",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 5. Product Grid or Empty State
    if (products.isEmpty()) {
      item(span = { GridItemSpan(2) }) {
        EmptySearchResults(
          query = searchQuery,
          category = selectedCategory,
          onReset = {
            viewModel.searchQuery.value = ""
            viewModel.selectedCategory.value = "All"
          }
        )
      }
    } else {
      items(
        items = products,
        key = { it.id }
      ) { product ->
        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
          ProductCard(
            product = product,
            isFavorite = product.id in favoriteIds,
            onCardClick = { viewModel.openProductDetail(product) },
            onFavoriteClick = { viewModel.toggleFavorite(product.id) },
            onAddToCartClick = { viewModel.addToCart(product) }
          )
        }
      }
    }
  }
}

@Composable
private fun HeroBannerCard() {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    shape = RoundedCornerShape(20.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(170.dp)
    ) {
      Image(
        painter = painterResource(id = R.drawable.marketplace_hero),
        contentDescription = "Secondhand Marketplace Banner",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      // Gradient overlay for contrast
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.horizontalGradient(
              colors = listOf(
                PineGreenDark.copy(alpha = 0.88f),
                PineGreen.copy(alpha = 0.65f),
                Color.Transparent
              )
            )
          )
      )

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp),
        verticalArrangement = Arrangement.Center
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Terracotta
        ) {
          Text(
            text = "CIRCULAR ECONOMY",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Renewed Stories.\nZero Waste.",
          color = Color.White,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Save up to 70% off retail while keeping goods in use.",
          color = Color.White.copy(alpha = 0.9f),
          fontSize = 12.sp
        )
      }
    }
  }
}

@Composable
private fun EmptySearchResults(
  query: String,
  category: String,
  onReset: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 48.dp, horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "🔍",
      fontSize = 44.sp
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = "No items found",
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = if (query.isNotBlank()) "No listings match \"$query\" in $category." else "No listings available in $category category.",
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
      onClick = onReset,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.testTag("reset_filters_button")
    ) {
      Text("Clear Filters & Show All")
    }
  }
}
