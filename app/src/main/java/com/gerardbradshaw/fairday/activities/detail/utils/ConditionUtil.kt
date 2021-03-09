package com.gerardbradshaw.fairday.activities.detail.utils

import com.gerardbradshaw.fairday.R
import com.github.matteobattilana.weather.PrecipType

abstract class ConditionUtil {
  companion object {
    @JvmStatic
    fun getConditionImageResId(conditionId: String?): Int {
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

    @JvmStatic
    fun getPrecipType(weatherId: Int?): PrecipType {
      if (weatherId == null) return PrecipType.CLEAR

      return when (weatherId) {
        200, 210, 500, 520 -> PrecipType.LIGHT_RAIN
        201, 211, 501, 511, 521, 532 -> PrecipType.RAIN
        202, 212, 221, 502, 522 -> PrecipType.HEAVY_RAIN
        230, 231, 232 -> PrecipType.RAIN
        in 300..321 -> PrecipType.DRIZZLE
        503 -> PrecipType.VERY_HEAVY_RAIN
        504 -> PrecipType.EXTREME_RAIN
        in 600..622 -> PrecipType.SNOW
        else -> PrecipType.CLEAR
      }
    }
  }
}