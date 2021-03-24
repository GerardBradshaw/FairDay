package com.gerardbradshaw.fairday.activities.detail.utils

import com.gerardbradshaw.fairday.retrofit.OneCallWeatherFile
import com.gerardbradshaw.weatherview.datamodels.WeatherData
import com.gerardbradshaw.fairday.retrofit.WeatherFile
import kotlin.math.roundToInt

object WeatherDataUtil {
  fun getWeatherDataFromWeatherFile(weatherFile: WeatherFile): WeatherData {
    return WeatherData(
      conditionName = weatherFile.weather?.get(0)?.main,
      conditionIconId = weatherFile.weather?.get(0)?.icon,
      conditionDescription = weatherFile.weather?.get(0)?.description,
      tempC = kelvinToCelsius(weatherFile.main?.temp),
      tempMinC = kelvinToCelsius(weatherFile.main?.tempMin),
      tempMaxC = kelvinToCelsius(weatherFile.main?.tempMax),
      humidity = weatherFile.main?.humidity,
      windSpeed = weatherFile.wind?.speed,
      windDirection = weatherFile.wind?.deg,
      cloudiness = weatherFile.clouds?.all,
      rainLastHour = weatherFile.rain?.mmLastHour,
      rainLastThreeHours = weatherFile.rain?.mmLastThreeHours,
      time = openWeatherTimeToUtc(weatherFile.updateTime),
      gmtOffset = openWeatherTimeToUtc(weatherFile.gmtOffset),
      sunrise = openWeatherTimeToUtc(weatherFile.sys?.sunrise),
      sunset = openWeatherTimeToUtc(weatherFile.sys?.sunset),
      locationName = weatherFile.name,
      latitude = weatherFile.coordinates?.lat,
      longitude = weatherFile.coordinates?.lon,
      weatherId = weatherFile.weather?.get(0)?.id)
  }

  fun getWeatherDataFromOneCallWeatherFile(weatherFile: OneCallWeatherFile): WeatherData {
    val hourlyData = getHourlyDataFromWeatherFile(weatherFile)
    val dailyData = getDailyDataFromWeatherFile(weatherFile)

    return WeatherData(
      conditionName = weatherFile.current?.weatherList?.get(0)?.main,
      conditionIconId = weatherFile.current?.weatherList?.get(0)?.icon,
      conditionDescription = weatherFile.current?.weatherList?.get(0)?.description,
      tempC = kelvinToCelsius(weatherFile.current?.temp),
      tempMinC = kelvinToCelsius(weatherFile.dailyList?.get(0)?.temp?.min),
      tempMaxC = kelvinToCelsius(weatherFile.dailyList?.get(0)?.temp?.max),
      humidity = weatherFile.current?.humidity,
      windSpeed = weatherFile.current?.windSpeed,
      windDirection = weatherFile.current?.windDirection,
      cloudiness = weatherFile.current?.clouds,
      rainLastHour = weatherFile.current?.rain?.rainLastHour,
      rainLastThreeHours = null,
      time = openWeatherTimeToUtc(weatherFile.current?.dt),
      gmtOffset = openWeatherTimeToUtc(weatherFile.timezoneOffset),
      sunrise = openWeatherTimeToUtc(weatherFile.dailyList?.get(0)?.sunrise),
      sunset = openWeatherTimeToUtc(weatherFile.dailyList?.get(0)?.sunset),
      locationName = "",
      latitude = weatherFile.lat,
      longitude = weatherFile.lon,
      weatherId = weatherFile.current?.weatherList?.get(0)?.id,
      hourlyData = hourlyData,
      dailyData = dailyData)
  }

  private fun getHourlyDataFromWeatherFile(weatherFile: OneCallWeatherFile): ArrayList<WeatherData.Hourly> {
    val hourlyData = ArrayList<WeatherData.Hourly>()

    val iterator = weatherFile.hourlyList?.iterator()
    if (iterator != null) {
      while (iterator.hasNext()) {
        val hourFile = iterator.next()

        val hourly = WeatherData.Hourly(
          openWeatherTimeToUtc(hourFile.dt),
          kelvinToCelsius(hourFile.temp),
          hourFile.weatherList?.get(0)?.icon)

        hourlyData.add(hourly)
      }
    }

    return hourlyData
  }

  private fun getDailyDataFromWeatherFile(weatherFile: OneCallWeatherFile): ArrayList<WeatherData.Daily> {
    val dailyData = ArrayList<WeatherData.Daily>()

    val iterator = weatherFile.dailyList?.iterator()
    if (iterator != null) {
      while (iterator.hasNext()) {
        val dayFile = iterator.next()

        val day = WeatherData.Daily(
          openWeatherTimeToUtc(dayFile.dt),
          kelvinToCelsius(dayFile.temp?.min),
          kelvinToCelsius(dayFile.temp?.max),
          dayFile.weatherList?.get(0)?.icon)

        dailyData.add(day)
      }
    }

    return dailyData
  }

  private fun kelvinToCelsius(temp: Float?): Float? {
    return temp?.minus(273.15f)
  }

  private fun openWeatherTimeToUtc(time: Long?): Long? {
    return time?.times(1000)
  }
}