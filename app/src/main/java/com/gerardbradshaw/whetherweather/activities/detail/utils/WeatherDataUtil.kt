package com.gerardbradshaw.whetherweather.activities.detail.utils

import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.whetherweather.retrofit.WeatherFile

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
      longitude = weatherFile.coordinates?.lon)
  }
}