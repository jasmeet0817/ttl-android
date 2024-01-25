package com.jasmeet.ttlsleep

import java.util.Calendar

data class Time(val hour: Int, val minute: Int) {
    // Now a String, manually set
    override fun toString(): String = "$hour:$minute"

    companion object {
        fun fromString(timeStr: String): Time {
            val (hour, minute) = timeStr.split(":").map { it.toInt() }
            return Time(hour, minute)
        }
    }

    fun toCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, this.hour)
        calendar.set(Calendar.MINUTE, this.minute)
        return calendar
    }
}
