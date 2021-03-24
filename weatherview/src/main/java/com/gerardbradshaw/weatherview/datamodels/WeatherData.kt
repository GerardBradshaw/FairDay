package com.gerardbradshaw.weatherview.datamodels

/**
 * Represents the weather at a location.
 * @param condition Condition name (e.g. rain, snow, extreme, etc).
 * @param conditionIconId Condition icon ID.
 * @param description Condition description.
 * @param currentTemp Temperature in Celsius.
 * @param minTemp Minimum temperature in Celsius.
 * @param maxTemp Maximum temperature in Celsius.
 * @param humidity Humidity %.
 * @param windSpeed Wind speed in meters/sec.
 * @param windDirection Direction of wind in meteorological degrees.
 * @param cloudiness Cloudiness %.
 * @param rainLastHour Rainfall in the last hour in mm.
 * @param rainLastThreeHours Rainfall in the last 3 hours in mm.
 * @param timeUpdated Time data was last updated.
 * @param gmtOffset GMT offset.
 * @param sunrise Sunrise time UTC.
 * @param sunset Sunset time UTC.
 * @param name Location name.
 * @param latitude Latitude of location.
 * @param longitude Longitude of location.
 */
data class WeatherData constructor(
  val condition: String? = null,
  val conditionIconId: String? = null,
  val description: String? = null,
  val currentTemp: Int? = null,
  val minTemp: Int? = null,
  val maxTemp: Int? = null,
  val humidity: Float? = null,
  val windSpeed: Float? = null,
  val windDirection: Float? = null,
  val cloudiness: Float? = null,
  val rainLastHour: Float? = null,
  val rainLastThreeHours: Float? = null,
  val timeUpdated: Long? = null,
  val gmtOffset: Long? = null,
  val sunrise: Long? = null,
  val sunset: Long? = null,
  val name: String? = null,
  val latitude: Float? = null,
  val longitude: Float? = null,
  val weatherId: Int? = null
)