package com.gerardbradshaw.whetherweather.ui.savedlocations

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.room.LocationEntity

class LocationsListAdapter(context: Context):
    RecyclerView.Adapter<LocationsListAdapter.LocationViewHolder>() {
  
  private var locations: List<LocationEntity> = ArrayList()
  private val inflater = LayoutInflater.from(context)
  
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_saved_location, parent, false)
    return LocationViewHolder(itemView)
  }
  
  override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
    try {
      val data = locations[position]
      holder.textView.text = data.locationName
    
    } catch (e: IndexOutOfBoundsException) {
      Log.d(TAG, "onBindViewHolder: index out of bounds for i = $position")
    }
  }
  
  override fun getItemCount(): Int {
    return locations.size
  }
  
  fun setLocations(locations: List<LocationEntity>) {
    this.locations = locations
    notifyDataSetChanged()
  }
  
  companion object {
    private const val TAG = "LocationsListAdapter"
  }
  
  class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.list_item_saved_location_text_view)
  }
}