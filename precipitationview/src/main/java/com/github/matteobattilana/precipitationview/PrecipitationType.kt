package com.github.matteobattilana.precipitationview

/**
 * Created by Mitchell on 7/6/2017.
 */
enum class PrecipitationType : WeatherData {
  CLEAR {
    override val emissionRate: Float = 0f
    override val speed: Int = 0
    override val scaleFactor = 0f
  },
  SNOW {
    override val emissionRate: Float = 20f
    override val speed: Int = 250
    override val scaleFactor = 1f
  },
  RAIN {
    override val emissionRate: Float = 100f
    override val speed: Int = (SNOW.speed * EnumConstants.RAIN_SPEED_COEFFICIENT).toInt()
    override val scaleFactor = 1f
  },
  DRIZZLE {
    override val emissionRate: Float = RAIN.emissionRate * 0.2f
    override val speed: Int = RAIN.speed
    override val scaleFactor = RAIN.scaleFactor
  },
  LIGHT_RAIN {
    override val emissionRate: Float = RAIN.emissionRate * 0.5f
    override val speed: Int = RAIN.speed
    override val scaleFactor = RAIN.scaleFactor
  },
  HEAVY_RAIN {
    override val emissionRate: Float = RAIN.emissionRate * 1.25f
    override val speed: Int = RAIN.speed
    override val scaleFactor = RAIN.scaleFactor
  },
  VERY_HEAVY_RAIN {
    override val emissionRate: Float = RAIN.emissionRate * 1.5f
    override val speed: Int = RAIN.speed
    override val scaleFactor = RAIN.scaleFactor
  },
  EXTREME_RAIN {
    override val emissionRate: Float = RAIN.emissionRate * 2.5f
    override val speed: Int = RAIN.speed
    override val scaleFactor = RAIN.scaleFactor
  },
  CUSTOM
  {
    override val emissionRate: Float = 10f
    override val speed: Int = 250
    override val scaleFactor = 1f
  };

  @Suppress("LeakingThis") // enum types are actually final, this warning is incorrect. Check if fixed in next plugin update
  override val precipitationType: PrecipitationType = this
}

private object EnumConstants {
  const val RAIN_SPEED_COEFFICIENT = 5.5 / 1.5 // Rain falls on average 3.6666x faster than snow
}