package com.gerardbradshaw.whetherweather.activities.detail.utils

import com.gerardbradshaw.whetherweather.R

abstract class ConditionImageUtil {
  companion object {
    @JvmStatic
    fun getResId(conditionId: String?): Int {
      if (conditionId == null|| conditionId.length < 3) return R.drawable.img_blank

      val number = Integer.parseInt(conditionId.substring(0,2))
      val isDay = conditionId.substring(3) == "d"

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