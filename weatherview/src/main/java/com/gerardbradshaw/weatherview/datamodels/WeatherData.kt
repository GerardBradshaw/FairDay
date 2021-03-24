package com.gerardbradshaw.weatherview.datamodels

/**
 * Represents the weather at a location.
 * @param conditionName Condition name (e.g. rain, snow, extreme, etc).
 * @param conditionIconId Condition icon ID.
 * @param conditionDescription Condition description.
 * @param tempC Temperature in Celsius.
 * @param tempMinC Minimum temperature in Celsius.
 * @param tempMaxC Maximum temperature in Celsius.
 * @param humidity Humidity %.
 * @param windSpeed Wind speed in meters/sec.
 * @param windDirection Direction of wind in meteorological degrees.
 * @param cloudiness Cloudiness %.
 * @param rainLastHour Rainfall in the last hour in mm.
 * @param rainLastThreeHours Rainfall in the last 3 hours in mm.
 * @param time Time data was last updated.
 * @param gmtOffset GMT offset.
 * @param sunrise Sunrise time UTC.
 * @param sunset Sunset time UTC.
 * @param locationName Location name.
 * @param latitude Latitude of location.
 * @param longitude Longitude of location.
 * @param weatherId the ID of the weather conditions
 * @param hourlyData a list of weather conditions over the next few hours
 * @param dailyData a list of weather conditions over the next few days
 */
data class WeatherData constructor(
  val conditionName: String? = null,
  val conditionIconId: String? = null,
  val conditionDescription: String? = null,
  val tempC: Float? = null,
  val tempMinC: Float? = null,
  val tempMaxC: Float? = null,
  val humidity: Float? = null,
  val windSpeed: Float? = null,
  val windDirection: Float? = null,
  val cloudiness: Float? = null,
  val rainLastHour: Float? = null,
  val rainLastThreeHours: Float? = null,
  val time: Long? = null,
  val gmtOffset: Long? = null,
  val sunrise: Long? = null,
  val sunset: Long? = null,
  val locationName: String? = null,
  val latitude: Float? = null,
  val longitude: Float? = null,
  val weatherId: Int? = null,
  val hourlyData: List<Hourly>? = null,
  val dailyData: List<Daily>? = null
) {

  data class Hourly constructor(
    val time: Long? = null,
    val tempC: Float? = null,
    val conditionIconId: String? = null
  )

  data class Daily constructor(
    val time: Long? = null,
    val tempMinC: Float? = null,
    val tempMaxC: Float? = null,
    val conditionIconId: String? = null
  )
}