package com.gerardbradshaw.whetherweather.ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.room.LocationData
import com.gerardbradshaw.whetherweather.views.ConditionsView
import com.gerardbradshaw.whetherweather.views.TemperatureView
import com.gerardbradshaw.whetherweather.views.WeatherView

class LocationListAdapter(context: Context) :
  RecyclerView.Adapter<LocationListAdapter.LocationViewHolder>() {

  var dataSet: List<LocationData>? = null
    set(value) {
      field = value
      notifyDataSetChanged()
    }

  var currentLocation: LocationData? = null
    set(value) {
      field = value
      notifyDataSetChanged()
    }

  private val inflater = LayoutInflater.from(context)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_weather_view, parent, false)
    return LocationViewHolder(itemView, this)
  }

  override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
    if (position >= itemCount) {
      Log.d(TAG, "onBindViewHolder: invalid position index")
      return
    }

    val data =
      if (currentLocation != null) {
        if (position == 0) currentLocation
        else dataSet?.get(position - 1)

      } else dataSet?.get(position)

    if (data == null) Log.d(TAG, "onBindViewHolder: Binding null data")
    else Log.d(TAG, "onBindViewHolder: Binding ${data.location}")

    holder.weatherView.setLocation(data?.location)
    holder.weatherView.setTemperatures(data?.temp, data?.min, data?.max)
    holder.weatherView.setConditions(data?.condition, data?.description, data?.conditionIconId)
  }

  override fun getItemCount(): Int {
    return dataSet?.size ?: 0 + if (currentLocation != null) 1 else 0
  }

  class LocationViewHolder(itemView: View, val adapter: LocationListAdapter) :
    RecyclerView.ViewHolder(itemView) {

    val weatherView: WeatherView = itemView.findViewById(R.id.list_item_weather_view)
  }

  companion object {
    private const val TAG = "LocationListAdapter"
  }
}