package com.gerardbradshaw.weatherinfoview.datamodels

class WeatherData private constructor() {
  constructor(
    condition: String?,
    conditionIconId: String?,
    description: String?,
    currentTemp: Int?,
    minTemp: Int?,
    maxTemp: Int?,
    humidity: Float?,
    windSpeed: Float?,
    windDirection: Float?,
    cloudiness: Float?,
    rainLastHour: Float?,
    rainLastThreeHours: Float?,
    timeUpdated: Long?,
    gmtOffset: Long?,
    sunrise: Long?,
    sunset: Long?,
    locationName: String?,
    latitude: Float?,
    longitude: Float?
  ) : this() {
    this.condition = condition
    this.conditionIconId = conditionIconId
    this.description = description
    this.currentTemp = currentTemp
    this.minTemp = minTemp
    this.maxTemp = maxTemp
    this.humidity = humidity
    this.windSpeed = windSpeed
    this.windDirection = windDirection
    this.cloudiness = cloudiness
    this.rainLastHour = rainLastHour
    this.rainLastThreeHours = rainLastThreeHours
    this.timeUpdated = timeUpdated
    this.gmtOffset = gmtOffset
    this.sunrise = sunrise
    this.sunset = sunset
    this.name = locationName
    this.latitude = latitude
    this.longitude = longitude
  }

  /** Group of weather parameters (e.g. Rain, Snow, Extreme, etc.). */
  var condition: String? = ""
    private set

  /** Weather icon ID */
  var conditionIconId: String? = ""
    private set

  /** Weather condition within the group. */
  var description: String? = ""
    private set

  /** Temperature in Celsius. */
  var currentTemp: Int? = 0
    private set

  /** Minimum temperature in Celsius. */
  var minTemp: Int? = 0
    private set

  /** Maximum temperature in Celsius. */
  var maxTemp: Int? = 0
    private set

  /** Humidity %. */
  var humidity: Float? = 0f
    private set

  /** Wind speed in meters/sec. */
  var windSpeed: Float? = 0f
    private set

  /** Direction of wind in meteorological degrees. */
  var windDirection: Float? = 0f
    private set

  /** Cloudiness %. */
  var cloudiness: Float? = 0f
    private set

  /** Rainfall in the last hour in mm. */
  var rainLastHour: Float? = 0f
    private set

  /** Rainfall in the last 3 hours in mm. */
  var rainLastThreeHours: Float? = 0f
    private set

  /** Time data was last updated. */
  var timeUpdated: Long? = 0L
    private set

  /** GMT offset */
  var gmtOffset: Long? = 0L
    private set

  /** Sunrise time UTC */
  var sunrise: Long? = 0L
    private set

  /** Sunset time UTC */
  var sunset: Long? = 0L
    private set

  /** Location name. */
  var name: String? = ""
    private set

  var latitude: Float? = 0f
    private set

  var longitude: Float? = 0f
    private set


  companion object {
    @JvmStatic
    fun getEmptyInstance(): WeatherData = WeatherData()
  }
}