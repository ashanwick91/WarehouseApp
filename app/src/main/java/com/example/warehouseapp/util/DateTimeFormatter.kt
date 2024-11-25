package com.example.warehouseapp.util

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object DateTimeFormatter {

    fun formatOffsetDateTime(offsetDateTime: OffsetDateTime): String {
        // Define your desired format (e.g., "dd MMM yyyy, hh:mm a")
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        // Format the OffsetDateTime
        return offsetDateTime.format(formatter)
    }
}
