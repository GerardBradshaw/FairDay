package com.gerardbradshaw.fairday.activities.detail.utils

import com.gerardbradshaw.fairday.R
import com.github.matteobattilana.precipitationview.PrecipitationType

abstract class ConditionUtil {

  private data class ConditionInfo(val idNumber: Int, val isDay: Boolean) {
    override fun toString() = "$idNumber${if (isDay) 'd' else 'n'}"
  }

  companion object {
    @JvmStatic
    private fun getConditionInfo(conditionId: String?): ConditionInfo? {
      if (conditionId == null) return null

      val idNumber = Integer.parseInt(conditionId.substring(0,2))
      val isDay = conditionId.last() == 'd'

      return ConditionInfo(idNumber, isDay)

        /*
        Codes:
          1 -> clear sky
          2 -> few clouds
          3 -> scattered clouds
          4 -> broken clouds
          9 -> shower rain
          10 -> rain
          11 -> thunderstorm
          13 -> snow
          50 -> mist
       */
    }

    @JvmStatic
    fun getConditionImageResId(conditionId: String?): Int {
      val conditionInfo = getConditionInfo(conditionId) ?: return R.drawable.img_blank

      return if (conditionInfo.isDay) {
        when (conditionInfo.idNumber) {
          in 1..4 -> R.drawable.img_day_clear
          in 9..11 -> R.drawable.img_day_rain
          13 -> R.drawable.img_day_snow
          50 -> R.drawable.img_day_rain // mist
          else -> R.drawable.img_day_clear
        }
      } else {
        when (conditionInfo.idNumber) {
          in 1..4 -> R.drawable.img_night_clear
          in 9..11 -> R.drawable.img_night_rain
          13 -> R.drawable.img_night_snow
          50 -> R.drawable.img_night_rain // mist
          else -> R.drawable.img_night_clear
        }
      }


    }

    @JvmStatic
    @Deprecated("Use getConditionImageResId() instead.")
    fun getConditionPhotoResId(conditionId: String?): Int {
      val conditionInfo = getConditionInfo(conditionId) ?: return R.drawable.img_blank

      return when (conditionInfo.idNumber) {
        2, 4 -> if (conditionInfo.isDay) R.drawable.img_broken_and_few_clouds_day else R.drawable.img_broken_and_few_clouds_night
        3 -> if (conditionInfo.isDay) R.drawable.img_scattered_clouds_day else R.drawable.img_scattered_clouds_night
        9 -> if (conditionInfo.isDay) R.drawable.img_shower_day else R.drawable.img_shower_night
        10 -> R.drawable.img_rain_both
        11 -> R.drawable.img_storm_both
        13 -> if (conditionInfo.isDay) R.drawable.img_snow_day else R.drawable.img_snow_night
        50 -> if (conditionInfo.isDay) R.drawable.img_clear_day else R.drawable.img_clear_night
        else -> if (conditionInfo.isDay) R.drawable.img_mist_day else R.drawable.img_mist_night
      }
    }

    @JvmStatic
    fun getPrecipitationType(weatherId: Int?): PrecipitationType {
      if (weatherId == null) return PrecipitationType.CLEAR

      return when (weatherId) {
        200, 210, 500, 520 -> PrecipitationType.LIGHT_RAIN
        201, 211, 501, 511, 521, 532 -> PrecipitationType.RAIN
        202, 212, 221, 502, 522 -> PrecipitationType.HEAVY_RAIN
        230, 231, 232 -> PrecipitationType.RAIN
        in 300..321 -> PrecipitationType.DRIZZLE
        503 -> PrecipitationType.VERY_HEAVY_RAIN
        504 -> PrecipitationType.EXTREME_RAIN
        in 600..622 -> PrecipitationType.SNOW
        else -> PrecipitationType.CLEAR
      }
    }

    @JvmStatic
    fun getCloudType(conditionId: String?): CloudType {
      val conditionInfo = getConditionInfo(conditionId) ?: return CloudType.CLEAR

      return when (conditionInfo.idNumber) {
        1 -> CloudType.CLEAR
        2 -> CloudType.FEW
        3 -> CloudType.SCATTERED
        4 -> CloudType.BROKEN
        9 -> CloudType.SHOWER
        10 -> CloudType.RAIN
        11 -> CloudType.THUNDERSTORM
        else -> CloudType.CLEAR
      }
    }

    private const val TAG = "GGG ConditionUtil"
  }
}