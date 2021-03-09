package com.gerardbradshaw.fairday.activities.detail.viewpager

import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.fairday.room.LocationEntity

class DetailPagerItem(
  var locationEntity: LocationEntity,
  var weatherData: WeatherData?,
  var isCurrentLocation: Boolean = false
  ) {

  override fun equals(other: Any?): Boolean {
    return other is LocationEntity && locationEntity == other
  }

  override fun hashCode(): Int {
    return locationEntity.hashCode() + weatherData.hashCode()
  }

  override fun toString(): String {
    return "Adapter item ${locationEntity.locality}"
  }
}