package ru.crew.motley.piideo.util

import java.util.*

/**
 * Created by Aleksandr on 002 02.02.18.
 */
class TimeUtils {
    companion object {

        fun gmtTimeInMillis(): Long {
            val tz = TimeZone.getTimeZone("GMT")
            return Date().time - tz.rawOffset
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
    }
}