package com.gerardbradshaw.whetherweather.ui.weather

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.models.WeatherData
import com.gerardbradshaw.whetherweather.views.WeatherView

class LocationListAdapter(context: Context) :
  RecyclerView.Adapter<LocationListAdapter.LocationViewHolder>() {

  private var weatherDataMap = LinkedHashMap<String, WeatherData>()
  private var currentLocationKey: String? = null
  private val inflater = LayoutInflater.from(context)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_weather_view, parent, false)
    return LocationViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
    val isCurrentLocation = position == 0 && currentLocationKey != null

    try {
      val data = weatherDataMap.values.elementAt(position)
      holder.weatherView.setLocation(data, isCurrentLocation)

    } catch (ioobException: IndexOutOfBoundsException) {
      Log.d(TAG, "onBindViewHolder: index out of bounds for i = $position")
    }
  }

  override fun getItemCount(): Int {
    return weatherDataMap.size
  }

  fun addLocations(vararg locations: WeatherData) {
    for (location in locations) {
      val name = location.locationName ?: "Unknown location"
      if (!weatherDataMap.containsKey(name)) weatherDataMap[name] = location
    }
    notifyDataSetChanged()
  }

  fun setCurrentLocation(location: WeatherData) {
    val currentLocationName = location.locationName ?: "Unknown location"

    val newMap = LinkedHashMap<String, WeatherData>()
    newMap[currentLocationName] = location

    for (entry in weatherDataMap.entries) {
      if (entry.key != currentLocationName) newMap[entry.key] = entry.value
    }

    weatherDataMap = newMap
    currentLocationKey = currentLocationName
    notifyDataSetChanged()
  }

  fun removeCurrentLocation() {
    if (currentLocationKey != null) {
      weatherDataMap.remove(currentLocationKey)
      currentLocationKey = null
      notifyDataSetChanged()
    }
  }

  fun getConditionIdForPosition(position: Int): String? {
    return try {
      weatherDataMap.values.elementAt(position).conditionIconId
    } catch (ioobException: java.lang.IndexOutOfBoundsException) {
      Log.d(TAG, "getConditionIdForPosition: invalid position $position")
      null
    }
  }

  class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val weatherView: WeatherView = itemView.findViewById(R.id.list_item_weather_view)
  }

  companion object {
    private const val TAG = "LocationListAdapter"
  }
}