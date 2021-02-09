package com.gerardbradshaw.whetherweather.activities.detail.viewpager

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.whetherweather.activities.detail.utils.WeatherUtil
import com.gerardbradshaw.whetherweather.room.LocationEntity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class PagerItemUtil(
  activity: AppCompatActivity,
  liveLocations: LiveData<List<LocationEntity>>,
  liveWeather: LiveData<LinkedHashMap<LocationEntity, WeatherData>>,
  private val weatherUtil: WeatherUtil
) {
  private var dataCached = LinkedHashMap<LocationEntity, DetailPagerItem>()
  val dataLive = MutableLiveData<LinkedList<DetailPagerItem>>()

  init {
    liveLocations.observe(activity) {
      val newCache = LinkedHashMap<LocationEntity, DetailPagerItem>()

      for (location in it) {
        val weatherData = dataCached[location]?.weatherData
        if (weatherData == null) weatherUtil.requestWeatherFor(location)
        newCache[location] = dataCached[location] ?: DetailPagerItem(location, weatherData)
      }

      dataCached = newCache
      postUpdates()
    }

    liveWeather.observe(activity) {
      for (entry in it) {
        val location = entry.key
        val weather = entry.value

        dataCached[location] =
          if (dataCached[location]?.weatherData == weather) dataCached[location]!!
          else DetailPagerItem(location, weather)
      }

      postUpdates()
    }
  }

  fun setWeather(location: LocationEntity, weather: WeatherData) {
    val isCurrentLocation = dataCached[location]?.isCurrentLocation ?: false
    dataCached[location] = DetailPagerItem(location, weather, isCurrentLocation)
    postUpdates()
  }

  fun setCurrentLocation(currentLocation: LocationEntity) {
    val newCache = LinkedHashMap<LocationEntity, DetailPagerItem>()

    newCache[currentLocation] = DetailPagerItem(currentLocation, null, true)

    for (entry in dataCached) {
      val location = entry.key
      val item = entry.value

      if (!item.isCurrentLocation) newCache[location] = item
    }

    dataCached = newCache
    postUpdates()

    if (dataCached[currentLocation]?.weatherData == null) {
      weatherUtil.requestWeatherFor(currentLocation)
    }
  }

  fun disableCurrentLocation() {
    val removeList = ArrayList<LocationEntity>()

    for (entry in dataCached) {
      if (entry.value.isCurrentLocation) {
        removeList.add(entry.key)
      }
    }

    if (removeList.isNotEmpty()) {
      for (r in removeList) {
        dataCached.remove(r)
      }
      postUpdates()
    }
  }

  private fun postUpdates() {
    dataLive.value = LinkedList(dataCached.values.toList())
  }
}