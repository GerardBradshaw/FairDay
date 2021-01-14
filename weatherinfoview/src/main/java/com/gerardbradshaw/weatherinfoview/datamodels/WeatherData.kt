package com.gerardbradshaw.weatherinfoview.datamodels

class WeatherData(
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
) {

    /** Group of weather parameters (e.g. Rain, Snow, Extreme, etc.). */
    val condition: String?

    /** Weather icon ID */
    val conditionIconId: String?

    /** Weather condition within the group. */
    val description: String?

    /** Temperature in Celsius. */
    val currentTemp: Int?

    /** Minimum temperature in Celsius. */
    val minTemp: Int?

    /** Maximum temperature in Celsius. */
    val maxTemp: Int?

    /** Humidity %. */
    val humidity: Float?

    /** Wind speed in meters/sec. */
    val windSpeed: Float?

    /** Direction of wind in meteorological degrees. */
    val windDirection: Float?

    /** Cloudiness %. */
    val cloudiness: Float?

    /** Rainfall in the last hour in mm. */
    val rainLastHour: Float?

    /** Rainfall in the last 3 hours in mm. */
    val rainLastThreeHours: Float?

    /** Time data was last updated. */
    val timeUpdated: Long?

    /** GMT offset */
    val gmtOffset: Long?

    /** Sunrise time UTC */
    val sunrise: Long?

    /** Sunset time UTC */
    val sunset: Long?

    /** Location name. */
    val locationName: String?

    val latitude: Float?
    val longitude: Float?

    init {
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
        this.locationName = locationName
        this.latitude = latitude
        this.longitude = longitude
    }
}