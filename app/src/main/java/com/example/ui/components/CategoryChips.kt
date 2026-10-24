package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories

@Composable
fun CategoryChips(
  selectedCategory: String,
  onSelectCategory: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val scrollState = rememberScrollState()

  Row(
    modifier = modifier
      .horizontalScroll(scrollState)
      .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Categories.ALL.forEach { category ->
      val isSelected = category == selectedCategory
      val icon = when (category) {
        "All" -> "✨"
        "Fashion" -> "🧥"
        "Electronics" -> "📷"
        "Home & Living" -> "🛋️"
        "Vintage" -> "🕰️"
        "Books" -> "📚"
        "Collectibles" -> "⌚"
        else -> "🏷️"
      }

      FilterChip(
        selected = isSelected,
        onClick = { onSelectCategory(category) },
        label = {
          Text(
            text = "$icon $category",
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
          containerColor = MaterialTheme.colorScheme.surface,
          labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
          selectedContainerColor = MaterialTheme.colorScheme.primary,
          selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
          enabled = true,
          selected = isSelected,
          borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
          selectedBorderColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.testTag("category_chip_$category")
      )
    }
  }
}
