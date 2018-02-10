package ru.crew.motley.piideo.util

import java.util.*

/**
 * Created by Aleksandr on 002 02.02.18.
 */
class TimeUtils {
    companion object {

        fun gmtTimeInMillis(): Long {
            val current = Date()
            val tz = TimeZone.getDefault()
            val offset = tz.rawOffset
            val getOffset = tz.getOffset(current.time)
            getOffset
            val st = System.currentTimeMillis()
            st
            val result = current.time - offset
//            return result
//            return System.currentTimeMillis()
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

//        fun gmtTimeInMillis(): Long {
//            val current = Date()
//            val tz = TimeZone.getDefault()
//            val offset = tz.rawOffset
//            val getOffset = tz.getOffset(current.time)
//            getOffset
//            val result = current.time - offset
//            return result
//        }
    }
}