package com.gerardbradshaw.whetherweather.ui.detail

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.weatherinfoview.WeatherInfoView
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.room.LocationEntity
import java.util.*

class DetailPagerAdapter(private val context: Context) :
    RecyclerView.Adapter<DetailPagerAdapter.DetailViewHolder>()
{
  private val inflater = LayoutInflater.from(context)
  private var locationEntityList = LinkedList<LocationEntity>()
  private var weatherDataList = LinkedList<WeatherData?>()
  private var isCurrentLocationEnabled = false


  // ------------------------ ADAPTER FUNCTIONS ------------------------

  override fun getItemCount(): Int {
    return locationEntityList.size
  }

  fun addNewLocation(locationEntity: LocationEntity) {
    if (locationEntityList.contains(locationEntity)) { // TODO HERE
      Log.d(TAG, "addNewLocation: ERROR: ${locationEntity.name} already exists and cannot be added.")
      return
    }

    locationEntityList.add(locationEntity)
    weatherDataList.add(null)
    notifyItemInserted(locationEntityList.indexOf(locationEntity))
    Log.d(TAG, "addNewLocation: ${locationEntity.name} added.")
  }

  fun setCurrentLocation(locationEntity: LocationEntity) {
    if (isCurrentLocationEnabled) {
      if (locationEntityList[0] != locationEntity) {
        locationEntityList[0] = locationEntity
        weatherDataList[0] = null
        notifyItemChanged(0)
      }
    } else {
      isCurrentLocationEnabled = true
      locationEntityList.addFirst(locationEntity)
      weatherDataList.addFirst(null)
      notifyItemInserted(0)
    }
  }

  fun removeCurrentLocation() {
    if (!isCurrentLocationEnabled) return

    isCurrentLocationEnabled = false
    locationEntityList.removeFirst()
    weatherDataList.removeFirst()
    notifyItemRemoved(0)
  }

  fun getConditionIdForPosition(position: Int): String? {
    return getWeatherDataFor(position)?.conditionIconId
  }

  fun updateWeatherData(
      weatherData: WeatherData,
      locationEntity: LocationEntity?
  ) {
    if (locationEntity == null) {
      Log.d(TAG, "updateWeatherData: ERROR: LocationEntity was null.")
      return
    }

    val locationIndex = locationEntityList.indexOf(locationEntity)
    if (locationIndex == -1) {
      Log.d(TAG, "updateWeatherData: ERROR: ${locationEntity.name} could not be found.")
      return
    }

    try {
      weatherDataList[locationIndex] = weatherData
      notifyItemChanged(locationIndex)
    } catch (e: IndexOutOfBoundsException) {
      Log.d(TAG, "updateWeatherData: ERROR: weather data could not be saved.")
    }
  }


  // ------------------------ INNER HELPERS ------------------------

  private fun getLocationEntityFor(position: Int): LocationEntity? {
    return try {
      locationEntityList.elementAt(position)
    } catch (e: IndexOutOfBoundsException) {
      Log.d(TAG, "getLocationEntityFor: ERROR: invalid position $position.")
      null
    }
  }

  private fun getWeatherDataFor(position: Int): WeatherData? {
    return try {
      weatherDataList.elementAt(position)
    } catch (e: IndexOutOfBoundsException) {
      Log.d(TAG, "getWeatherDataFor: ERROR: invalid position $position.")
      null
    }
  }


  // ------------------------ VIEW HOLDER ------------------------

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_weather_detail, parent, false)
    return DetailViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
    try {
      val weatherData = weatherDataList[position] // TODO HERE
      val isCurrentLocation = position == 0 && isCurrentLocationEnabled

      if (weatherData == null) {
        holder.weatherView.setLocationName(context.getString(R.string.string_loading), isCurrentLocation)
      } else {
        holder.weatherView.setLocation(weatherData, isCurrentLocation)
      }
    } catch (e: IndexOutOfBoundsException) {
      Log.d(TAG, "onBindViewHolder: ERROR: invalid position")
    }
  }

  class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val weatherView: WeatherInfoView = itemView.findViewById(R.id.list_item_weather_view)
  }

  companion object {
    private const val TAG = "GGG LocationListAdapter"
  }
}