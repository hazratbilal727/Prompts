package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Distinct solid color pairs used to generate soft linear gradient backgrounds
 * at 20% opacity with no shadows and no borders.
 */
val CARD_GRADIENT_PAIRS = listOf(
    // 0: Indigo to Royal Blue
    Pair(Color(0xFF4338CA), Color(0xFF2563EB)),
    // 1: Emerald to Teal
    Pair(Color(0xFF059669), Color(0xFF0D9488)),
    // 2: Rose to Crimson
    Pair(Color(0xFFE11D48), Color(0xFFF43F5E)),
    // 3: Amber to Warm Orange
    Pair(Color(0xFFD97706), Color(0xFFEA580C)),
    // 4: Violet to Purple
    Pair(Color(0xFF7C3AED), Color(0xFF9333EA)),
    // 5: Cyan to Sky Blue
    Pair(Color(0xFF0284C7), Color(0xFF06B6D4)),
    // 6: Coral to Tangerine
    Pair(Color(0xFFEA580C), Color(0xFFFB923C)),
    // 7: Fuchsia to Magenta
    Pair(Color(0xFFC026D3), Color(0xFFE879F9)),
    // 8: Forest Green to Lime
    Pair(Color(0xFF16A34A), Color(0xFF65A30D)),
    // 9: Deep Teal to Ocean Blue
    Pair(Color(0xFF0F766E), Color(0xFF0284C7)),
    // 10: Plum to Raspberry
    Pair(Color(0xFF831843), Color(0xFFBE123C)),
    // 11: Electric Blue to Iris
    Pair(Color(0xFF1D4ED8), Color(0xFF6366F1))
)

/**
 * Returns a soft solid linear gradient brush with exact 20% opacity (alpha = 0.20f)
 * deterministically based on the prompt ID.
 */
fun getPromptCardGradient(id: Long, opacity: Float = 0.20f): Brush {
    val positiveId = id.toInt() and 0x7FFFFFFF
    val index = positiveId % CARD_GRADIENT_PAIRS.size
    val (startColor, endColor) = CARD_GRADIENT_PAIRS[index]
    return Brush.linearGradient(
        colors = listOf(
            startColor.copy(alpha = opacity),
            endColor.copy(alpha = opacity)
        )
    )
}

/**
 * Returns a soft solid linear gradient brush with exact 20% opacity by index.
 */
fun getCardGradientByIndex(index: Int, opacity: Float = 0.20f): Brush {
    val positiveIndex = (index and 0x7FFFFFFF) % CARD_GRADIENT_PAIRS.size
    val (startColor, endColor) = CARD_GRADIENT_PAIRS[positiveIndex]
    return Brush.linearGradient(
        colors = listOf(
            startColor.copy(alpha = opacity),
            endColor.copy(alpha = opacity)
        )
    )
}
