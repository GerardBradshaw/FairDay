package com.gerardbradshaw.weatherview

import java.util.*

internal object WeatherViewUtils {
  fun getTimeString(time: Long?, gmtOffset: Long? = null): String {
    if (time == null) return "-"

    with(Calendar.getInstance()) {
      val localGmtOffset = timeZone.getOffset(timeInMillis)
      timeInMillis = time + if (gmtOffset != null) gmtOffset - localGmtOffset else 0

      val minute = get(Calendar.MINUTE)
      val minuteString = if (minute < 10) "0$minute" else minute.toString()

      val hour = get(Calendar.HOUR)
      val hourString = if (hour == 0) "12" else hour.toString()

      val amPm = if (get(Calendar.AM_PM) == 0) "am" else "pm"

      return "$hourString:$minuteString $amPm"
      // TODO use device setting (12 or 24 hour time)
    }
  }

  /**
   * Returns the hour (e.g. 6 am) for a given time. Does not round up (e.g. an input of 6:40 pm will
   * return 6 pm (not 7 pm).
   */
  fun getTimeStringHourOnly(time: Long?, gmtOffset: Long? = null): String {
    if (time == null) return "-"

    with(Calendar.getInstance()) {
      val localGmtOffset = timeZone.getOffset(timeInMillis)
      timeInMillis = time + if (gmtOffset != null) gmtOffset - localGmtOffset else 0

      val hour = get(Calendar.HOUR)
      val hourString = if (hour == 0) "12" else hour.toString()

      val amPm = if (get(Calendar.AM_PM) == 0) "am" else "pm"

      return "$hourString$amPm"
      // TODO use device setting (12 or 24 hour time)
    }
  }
}