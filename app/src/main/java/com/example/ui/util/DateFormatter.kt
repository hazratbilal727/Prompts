package com.example.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val fullDateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun formatFull(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    fun formatRelative(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / (60 * 1000)
        val hours = diff / (60 * 60 * 1000)
        val days = diff / (24 * 60 * 60 * 1000)

        return when {
            diff < 60 * 1000 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> shortDateFormat.format(Date(timestamp))
        }
    }

    fun formatShort(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }
}
