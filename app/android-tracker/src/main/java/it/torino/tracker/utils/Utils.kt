/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.utils

import android.os.SystemClock
import it.torino.tracker.utils.Globals.Companion.MSECS_IN_AN_HOUR
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Utils {
    companion object {
        fun millisecondsToString(msecs: Long, format: String): String? {
            val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
            val date = Date()
            date.time = msecs
            return simpleDateFormat.format(date)
        }

        fun getTMZoneOffset(time: Long): Int {
            return TimeZone.getDefault().getOffset(time)
        }


        private fun calendarWithLocalTimeZone(msecs: Long): Calendar {
            val tmz = TimeZone.getDefault()
            val calendar = Calendar.getInstance(tmz)
            calendar.timeInMillis = msecs
            return calendar
        }

        /**
         * get the midnight of the day
         */
        fun midnightinMsecs(timeInMsecs: Long): Long {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = timeInMsecs
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return calendar.timeInMillis
        }

        /**
         * it gets the epoch time (time in MSecs) using the event time that is relative to the last time
         * the phone was rebooted
         * @param timestamp is time is in nanoseconds it represents the set reference times the first time we come here
         * @return the event timestamp in milliseconds from epoch
         * see answer 2 at http://stackoverflow.com/questions/5500765/accelerometer-sensorevent-timestamp
         */
        fun fromEventTimeToEpoch(timestamp: Long): Long {
            // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
            val timePhoneWasLastRebooted =
                System.currentTimeMillis() - SystemClock.elapsedRealtime()
            return timePhoneWasLastRebooted + (timestamp / 1000000.0).toLong()
        }

        /**
         * it gets a time in msecs and returns the rounded time in seconds         *
         * @param timeInMSecs
         * @return
         */
        fun getTimeInSeconds(timeInMSecs: Long): Long {
            return (timeInMSecs / 1000).toLong()
        }

        /**
         * give a duration in msecs prints the duration in teh form of
         *    HH : mm : ss
         *    or mm : ss
         *    or ss Secs
         *
         * @param millis
         * @return
         */
        fun millisecondsToDuration(millis: Long): CharSequence {
            val hours = TimeUnit.MILLISECONDS.toHours(millis)

            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(millis)
            )
            val minStr =
                if (hours == 0L && minutes == 0L) "" else if (minutes > 9) minutes.toString() else "0$minutes"
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(millis)
            )
            var secStr = if (seconds > 9) seconds.toString() else "0$seconds"
            var finStr = if (hours == 0L) "" else "$hours:"
            if (minStr == "") {
                finStr = "$secStr Secs"
            } else {
                finStr += "$minStr:$secStr"
            }
            return finStr
        }


        /**
         * it tells if a time in msecs is from today
         */
        fun isToday(millis: Long): Boolean {
            val now = Calendar.getInstance()
            val comparisonDate = Calendar.getInstance().apply {
                timeInMillis = millis
            }
            return now[Calendar.DAY_OF_YEAR] == comparisonDate[Calendar.DAY_OF_YEAR] &&
                    now[Calendar.YEAR] == comparisonDate[Calendar.YEAR]
        }
    }


    /**
     * @return a timestring of the current time
     */
    fun getCurrentDateTimeString(): String {
        val date = Date()
        val formatter = SimpleDateFormat("yyyy_MM_dd-HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }
}