package com.example.imagetopdf

import android.text.format.DateFormat
import java.util.*

object Methods {
    fun formatTimestamp(timestamp: Long): String{
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy", calendar).toString()

    }
}