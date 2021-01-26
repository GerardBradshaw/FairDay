package com.gerardbradshaw.whetherweather.util.conditions

import com.gerardbradshaw.whetherweather.R

abstract class ConditionImageUtil {
  companion object {
    @JvmStatic
    fun getConditionImageUri(conditionIconId: String?): Int {
      if (conditionIconId == null|| conditionIconId.length < 3) return R.drawable.img_clear_day

      val number = Integer.parseInt(conditionIconId.substring(0,2))
      val isDay = conditionIconId.substring(3) == "d"

      return when (number) {
        2, 4 -> if (isDay) R.drawable.img_broken_and_few_clouds_day else R.drawable.img_broken_and_few_clouds_night
        3 -> if (isDay) R.drawable.img_scattered_clouds_day else R.drawable.img_scattered_clouds_night
        9 -> if (isDay) R.drawable.img_shower_day else R.drawable.img_shower_night
        10 -> R.drawable.img_rain_both
        11 -> R.drawable.img_storm_both
        13 -> if (isDay) R.drawable.img_snow_day else R.drawable.img_snow_night
        50 -> if (isDay) R.drawable.img_clear_day else R.drawable.img_clear_night
        else -> if (isDay) R.drawable.img_mist_day else R.drawable.img_mist_night
      }
    }
  }
}