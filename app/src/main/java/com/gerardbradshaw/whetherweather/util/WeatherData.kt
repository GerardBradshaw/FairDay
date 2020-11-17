package com.gerardbradshaw.whetherweather.util

import com.gerardbradshaw.whetherweather.retrofit.WeatherFile

class WeatherData(weatherFile: WeatherFile) {

  /** Group of weather parameters (e.g. Rain, Snow, Extreme, etc.). */
  val condition = weatherFile.weather?.get(0)?.main

  /** Weather condition within the group. */
  val description = weatherFile.weather?.get(0)?.description

  /** Temperature in Celsius. */
  val temp = weatherFile.main?.temp?.minus(273.15)?.toInt()

  /** Minimum temperature in Celsius. */
  val min = weatherFile.main?.tempMin?.minus(273.15)?.toInt()

  /** Maximum temperature in Celsius. */
  val max = weatherFile.main?.tempMax?.minus(273.15)?.toInt()

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
  val timeUpdated: Int? = weatherFile.updateTime

  /** Sunrise time UTC */
  val gmtOffset: Int? = weatherFile.gmtOffset

  /** Sunrise time UTC */
  val sunrise: Int? = weatherFile.sys?.sunrise

  /** Sunset time UTC */
  val sunset: Int? = weatherFile.sys?.sunset

  /** Location name. */
  val location = weatherFile.name

  val latitude: Float? = weatherFile.coordinates?.lat

  val longitude: Float? = weatherFile.coordinates?.lon
}
