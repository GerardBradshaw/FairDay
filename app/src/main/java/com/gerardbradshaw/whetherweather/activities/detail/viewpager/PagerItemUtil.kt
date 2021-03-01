package com.gerardbradshaw.whetherweather.activities.detail.viewpager

import android.util.Log
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
    liveLocations.observe(activity) { syncLocations(it) }
    liveWeather.observe(activity) { syncWeather(it) }
  }

  private fun syncLocations(locations: List<LocationEntity>) {
    synchronized(this) {
      val newCache = LinkedHashMap<LocationEntity, DetailPagerItem>()
      val requestList = ArrayList<LocationEntity>()

      for (location in locations) {
        val weatherData = dataCached[location]?.weatherData
        if (weatherData == null) requestList.add(location)
        newCache[location] = dataCached[location] ?: DetailPagerItem(location, weatherData)
      }

      dataCached = newCache
      postUpdates()

      for (location in requestList) weatherUtil.requestWeatherFor(location)
    }
  }

  private fun syncWeather(weatherMap: LinkedHashMap<LocationEntity, WeatherData>) {
    synchronized(this) {
      for (entry in weatherMap) {
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
    synchronized(this) {
      val isCurrentLocation = dataCached[location]?.isCurrentLocation ?: false
      dataCached[location] = DetailPagerItem(location, weather, isCurrentLocation)
      postUpdates()
    }
  }

  fun refreshWeather() {
    synchronized(this) {
      for (key in dataCached.keys) {
        val isCurrentLocation = dataCached[key]?.isCurrentLocation ?: false
        dataCached[key] = DetailPagerItem(key, null, isCurrentLocation)
      }
      postUpdates()

      for (location in dataCached.keys) weatherUtil.requestWeatherFor(location)
    }
  }

  fun setCurrentLocation(currentLocation: LocationEntity) {
    synchronized(this) {
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
  }

  fun disableCurrentLocation() {
    synchronized(this) {
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
  }

  private fun postUpdates() {
    Log.d(TAG, "postUpdates: posting updates")
    dataLive.value = LinkedList(dataCached.values.toList())
  }

  companion object {
    private const val TAG = "GGG PagerItemUtil"
  }
}