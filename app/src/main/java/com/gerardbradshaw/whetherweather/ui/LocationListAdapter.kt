package com.gerardbradshaw.whetherweather.ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.room.LocationData
import com.gerardbradshaw.whetherweather.views.WeatherView

class LocationListAdapter(context: Context) :
  RecyclerView.Adapter<LocationListAdapter.LocationViewHolder>() {

  var locations: List<LocationData>? = null
    set(value) {
      field = value
      notifyDataSetChanged()
    }

  var currentLocation: LocationData? = null
    set(value) {
      val prevValue = field
      field = value

      when {
        value == null && prevValue == null -> return
        value == null && prevValue != null -> notifyItemRemoved(0)
        value != null && prevValue != null -> notifyItemChanged(0)
        value != null && prevValue == null -> notifyItemInserted(0)
        else -> notifyDataSetChanged()
      }
    }

  private val inflater = LayoutInflater.from(context)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_weather_view, parent, false)
    return LocationViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
    if (position >= itemCount) {
      Log.d(TAG, "onBindViewHolder: invalid position index")
      return
    }

    val isCurrentLocation = position == 0 && currentLocation != null

    val data =
      when {
        isCurrentLocation -> currentLocation
        currentLocation != null -> locations?.get(position - 1)
        else -> locations?.get(position)
      }

    if (data == null) Log.d(TAG, "onBindViewHolder: Binding null data")
    else Log.d(TAG, "onBindViewHolder: Binding ${data.locationName} at position $position")

    holder.weatherView.setLocation(data)
  }

  override fun getItemCount(): Int {
    return locations?.size ?: 0 + if (currentLocation != null) 1 else 0
  }

  class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val weatherView: WeatherView = itemView.findViewById(R.id.list_item_weather_view)
  }

  companion object {
    private const val TAG = "LocationListAdapter"
  }
}