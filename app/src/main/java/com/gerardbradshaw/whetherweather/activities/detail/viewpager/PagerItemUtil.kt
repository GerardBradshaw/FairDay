package com.gerardbradshaw.whetherweather.activities.detail.viewpager

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.whetherweather.activities.detail.utils.WeatherUtil
import com.gerardbradshaw.whetherweather.room.LocationEntity
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class PagerItemUtil(
  activity: AppCompatActivity,
  liveLocations: LiveData<List<LocationEntity>>,
  liveWeather: LiveData<LinkedHashMap<LocationEntity, WeatherData>>,
  private val weatherUtil: WeatherUtil
) {
  private var dataCached = LinkedHashMap<LocationEntity, DetailPagerItem>()
  val dataLive = MutableLiveData<ArrayList<DetailPagerItem>>()

  init {
    liveLocations.observe(activity) { syncLocations(it) }
    liveWeather.observe(activity) { syncWeather(it) }
  }

  private fun syncLocations(locations: List<LocationEntity>) {
    synchronized(this) {
      for (location in  dataCached.keys.toList()) {
        if (!locations.contains(location)) {
          dataCached.remove(location)
        }
      }

      val weatherRequestList = ArrayList<LocationEntity>()
      for (location in locations) {
        if (!dataCached.containsKey(location)) {
          dataCached[location] = DetailPagerItem(location, null)
          weatherRequestList.add(location)
        }
      }

      postUpdates()

      for (location in weatherRequestList) {
        weatherUtil.requestWeatherFor(location)
      }
    }
  }

  private fun syncWeather(weatherMap: LinkedHashMap<LocationEntity, WeatherData>) {
    synchronized(this) {
      for (entry in weatherMap) {
        val entity = entry.key
        val weatherData = entry.value

        dataCached[entity] =
          if (dataCached[entity]?.weatherData == weatherData) dataCached[entity]!!
          else DetailPagerItem(entity, weatherData)
      }

      postUpdates()
    }
  }

  fun setWeather(location: LocationEntity, weather: WeatherData) {
    synchronized(this) {
      if (dataCached.containsKey(location)) {
        dataCached[location]?.weatherData = weather
        postUpdates()
      } else {
        Log.e(TAG, "setWeather: ${location.locality} does not exist - no weather can be set.")
      }
    }
  }

  fun refreshWeather() {
    synchronized(this) {
      for (location in dataCached.keys) {
        dataCached[location]?.weatherData = null
      }
      postUpdates()

      for (location in dataCached.keys) weatherUtil.requestWeatherFor(location)
    }
  }

  fun setCurrentLocation(location: LocationEntity) {
    synchronized(this) {
      val isSameLocation = dataCached[location]?.isCurrentLocation ?: false

      if (isSameLocation) return
      else {
        val newCache = LinkedHashMap<LocationEntity, DetailPagerItem>()
        newCache[location] = DetailPagerItem(location, null, true)

        for (entry in dataCached) {
          if (newCache[entry.key]?.isCurrentLocation == true) continue
          newCache[entry.key] = entry.value
        }

        dataCached = newCache
        postUpdates()

        weatherUtil.requestWeatherFor(location)
      }
    }
  }

  fun disableCurrentLocation() {
    synchronized(this) {
      for (entry in dataCached) {
        if (entry.value.isCurrentLocation) {
          dataCached.remove(entry.key)
          postUpdates()
          break
        }
      }
    }
  }

  private fun postUpdates() {
    Log.d(TAG, "postUpdates: posting updates")
    dataLive.value = ArrayList(dataCached.values.toList())
  }

  companion object {
    private const val TAG = "GGG PagerItemUtil"
  }
}