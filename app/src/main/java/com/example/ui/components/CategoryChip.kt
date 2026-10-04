package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CategoryBusiness
import com.example.ui.theme.CategoryCoding
import com.example.ui.theme.CategoryDesign
import com.example.ui.theme.CategoryDev
import com.example.ui.theme.CategoryGeneral
import com.example.ui.theme.CategoryImageGen
import com.example.ui.theme.CategoryMarketing
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategoryResearch
import com.example.ui.theme.CategoryWriting

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "coding" -> CategoryCoding
        "writing" -> CategoryWriting
        "design" -> CategoryDesign
        "marketing" -> CategoryMarketing
        "business" -> CategoryBusiness
        "research" -> CategoryResearch
        "image generation" -> CategoryImageGen
        "development" -> CategoryDev
        "general" -> CategoryGeneral
        else -> CategoryOther
    }
}

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(category)
    val shape = RoundedCornerShape(8.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(categoryColor.copy(alpha = 0.12f))
            .border(1.dp, categoryColor.copy(alpha = 0.3f), shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(categoryColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = category,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = categoryColor
        )
    }
}

@Composable
fun CategoryFilterChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    count: Int? = null,
    modifier: Modifier = Modifier
) {
    val categoryColor = if (category.equals("All", ignoreCase = true)) {
        MaterialTheme.colorScheme.primary
    } else {
        getCategoryColor(category)
    }

    val shape = RoundedCornerShape(20.dp)
    val backgroundColor = if (isSelected) {
        categoryColor
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }
    val textColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!category.equals("All", ignoreCase = true)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else categoryColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = category,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = textColor
        )

        if (count != null && count > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textColor
                )
            }
        }
    }
}
