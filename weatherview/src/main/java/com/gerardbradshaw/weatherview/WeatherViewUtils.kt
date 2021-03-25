package com.gerardbradshaw.weatherview

import java.util.*

internal object WeatherViewUtils {
  fun getTimeString(time: Long?, gmtOffset: Long? = null): String {
    if (time == null) return "-"

    val cal = Calendar.getInstance()
    val localGmtOffset = cal.timeZone.getOffset(cal.timeInMillis)

    cal.timeInMillis = time + if (gmtOffset != null) gmtOffset - localGmtOffset else 0

    val minute = cal.get(Calendar.MINUTE)
    val minuteString = if (minute < 10) "0$minute" else "$minute"

    val amPm = if (cal.get(Calendar.AM_PM) == 0) "am" else "pm"

    return "${cal.get(Calendar.HOUR)}:$minuteString $amPm"
  }

  fun getHourOnlyTimeString(time: Long?, gmtOffset: Long? = null): String {
    if (time == null) return "-"

    val cal = Calendar.getInstance()
    val localGmtOffset = cal.timeZone.getOffset(cal.timeInMillis)

    cal.timeInMillis = time + if (gmtOffset != null) gmtOffset - localGmtOffset else 0

    val amPm = if (cal.get(Calendar.AM_PM) == 0) "am" else "pm"

    val hour = cal.get(Calendar.HOUR)

    return when {
      hour == 0 -> "12am"
      else -> "$hour$amPm"
    }
  }
}