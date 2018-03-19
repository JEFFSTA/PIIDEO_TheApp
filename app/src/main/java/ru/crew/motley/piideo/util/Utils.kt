package ru.crew.motley.piideo.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import java.util.*


/**
 * Created by Aleksandr on 002 02.02.18.
 */
class Utils {
    companion object {

        private val DEFAULT_LOCALE = Locale.FRENCH
        private val AVAILABLE_LOCALES = arrayOf(
                Locale.FRANCE,
                Locale.CANADA_FRENCH,
                Locale.FRENCH,
                Locale.ENGLISH,
                Locale.US,
                Locale.UK
        )

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


        fun invalidateCurrentLocale(context: Context) {
            updateResources(context,
                    getLocaleOrDefault(getConfigLocale(context.resources.configuration)))
        }

        private fun getConfigLocale(configuration: Configuration): Locale {
            return if (Build.VERSION.SDK_INT < 24)
                configuration.locale
            else
                configuration.locales.get(0)
        }

        private fun getLocaleOrDefault(locale: Locale): Locale {
            Log.d("LOCALE", "----" + locale.toString())
            Log.d("LOCALE", AVAILABLE_LOCALES.toString())
            return if (AVAILABLE_LOCALES.contains(locale))
                locale
            else DEFAULT_LOCALE
        }

        private fun updateResources(context: Context, locale: Locale) {
            Locale.setDefault(locale)

            val res = context.resources
            val config = Configuration(res.configuration)
            if (Build.VERSION.SDK_INT < 24)
                config.locale = locale
            else
                config.setLocale(locale)
            res.updateConfiguration(config, res.displayMetrics)
        }
    }
}