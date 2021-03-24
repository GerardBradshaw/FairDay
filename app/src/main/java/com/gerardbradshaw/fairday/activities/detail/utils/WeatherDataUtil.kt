package com.gerardbradshaw.fairday.activities.detail.utils

import com.gerardbradshaw.fairday.retrofit.OneCallWeatherFile
import com.gerardbradshaw.weatherview.datamodels.WeatherData
import com.gerardbradshaw.fairday.retrofit.WeatherFile

object WeatherDataUtil {
  fun getWeatherDataFromWeatherFile(weatherFile: WeatherFile): WeatherData {
    return WeatherData(
      condition = weatherFile.weather?.get(0)?.main,
      conditionIconId = weatherFile.weather?.get(0)?.icon,
      description = weatherFile.weather?.get(0)?.description,
      currentTemp = weatherFile.main?.temp?.minus(273.15)?.toInt(),
      minTemp = weatherFile.main?.tempMin?.minus(273.15)?.toInt(),
      maxTemp = weatherFile.main?.tempMax?.minus(273.15)?.toInt(),
      humidity = weatherFile.main?.humidity,
      windSpeed = weatherFile.wind?.speed,
      windDirection = weatherFile.wind?.deg,
      cloudiness = weatherFile.clouds?.all,
      rainLastHour = weatherFile.rain?.mmLastHour,
      rainLastThreeHours = weatherFile.rain?.mmLastThreeHours,
      timeUpdated = weatherFile.updateTime?.times(1000),
      gmtOffset = weatherFile.gmtOffset?.times(1000),
      sunrise = weatherFile.sys?.sunrise?.times(1000),
      sunset = weatherFile.sys?.sunset?.times(1000),
      name = weatherFile.name,
      latitude = weatherFile.coordinates?.lat,
      longitude = weatherFile.coordinates?.lon,
      weatherId = weatherFile.weather?.get(0)?.id)
  }

  fun getWeatherDataFromOneCallWeatherFile(weatherFile: OneCallWeatherFile): WeatherData {
    return WeatherData(
      condition = weatherFile.current?.weatherList?.get(0)?.main,
      conditionIconId = weatherFile.current?.weatherList?.get(0)?.icon,
      description = weatherFile.current?.weatherList?.get(0)?.description,
      currentTemp = weatherFile.current?.temp?.minus(273.15)?.toInt(),
      minTemp = weatherFile.dailyList?.get(0)?.temp?.min?.minus(273.15)?.toInt(),
      maxTemp = weatherFile.dailyList?.get(0)?.temp?.max?.minus(273.15)?.toInt(),
      humidity = weatherFile.current?.humidity,
      windSpeed = weatherFile.current?.windSpeed,
      windDirection = weatherFile.current?.windDirection,
      cloudiness = weatherFile.current?.clouds,
      rainLastHour = weatherFile.current?.rain?.rainLastHour,
      rainLastThreeHours = null,
      timeUpdated = weatherFile.current?.dt?.times(1000),
      gmtOffset = weatherFile.timezoneOffset?.times(1000),
      sunrise = weatherFile.dailyList?.get(0)?.sunrise?.times(1000),
      sunset = weatherFile.dailyList?.get(0)?.sunset?.times(1000),
      name = "",
      latitude = weatherFile.lat,
      longitude = weatherFile.lon,
      weatherId = weatherFile.current?.weatherList?.get(0)?.id)
  }
}