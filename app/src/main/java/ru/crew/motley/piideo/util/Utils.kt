package ru.crew.motley.piideo.util

import java.util.*

/**
 * Created by Aleksandr on 002 02.02.18.
 */
class Utils {
    companion object {

        fun gmtTimeInMillis(): Long {
            return Date().time
        }

        fun gmtDayTimestamp(timestamp: Long): Long {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MINUTE, 0)
            return calendar.timeInMillis
        }

        fun contains(arr: Array<Int>, value: Int) = arr.contains(value)
    }
}