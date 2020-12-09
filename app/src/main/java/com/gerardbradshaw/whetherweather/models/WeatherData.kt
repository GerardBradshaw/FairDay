package com.gerardbradshaw.whetherweather.models

import com.gerardbradshaw.whetherweather.retrofit.WeatherFile

class WeatherData(weatherFile: WeatherFile) {

  /** Group of weather parameters (e.g. Rain, Snow, Extreme, etc.). */
  val condition = weatherFile.weather?.get(0)?.main

  /** Weather icon ID */
  val conditionIconId = weatherFile.weather?.get(0)?.icon

  /** Weather condition within the group. */
  val description = weatherFile.weather?.get(0)?.description

  /** Temperature in Celsius. */
  val currentTemp = weatherFile.main?.temp?.minus(273.15)?.toInt()

  /** Minimum temperature in Celsius. */
  val minTemp = weatherFile.main?.tempMin?.minus(273.15)?.toInt()

  /** Maximum temperature in Celsius. */
  val maxTemp = weatherFile.main?.tempMax?.minus(273.15)?.toInt()

  /** Humidity %. */
  val humidity = weatherFile.main?.humidity

  /** Wind speed in meters/sec. */
  val windSpeed = weatherFile.wind?.speed

  /** Direction of wind in meteorological degrees. */
  val windDirection = weatherFile.wind?.deg

  /** Cloudiness %. */
  val cloudiness = weatherFile.clouds?.all

  /** Rainfall in the last hour in mm. */
  val rainLastHour = weatherFile.rain?.mmLastHour

  /** Rainfall in the last 3 hours in mm. */
  val rainLastThreeHours = weatherFile.rain?.mmLastThreeHours

  /** Time data was last updated. */
  val timeUpdated: Long? = weatherFile.updateTime?.times(1000)

  /** GMT offset */
  val gmtOffset: Long? = weatherFile.gmtOffset?.times(1000)

  /** Sunrise time UTC */
  val sunrise: Long? = weatherFile.sys?.sunrise?.times(1000)

  /** Sunset time UTC */
  val sunset: Long? = weatherFile.sys?.sunset?.times(1000)

  /** Location name. */
  val locationName = weatherFile.name

  val latitude: Float? = weatherFile.coordinates?.lat

  val longitude: Float? = weatherFile.coordinates?.lon
}
