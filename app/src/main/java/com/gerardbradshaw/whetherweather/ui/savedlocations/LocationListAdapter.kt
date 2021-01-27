package com.gerardbradshaw.whetherweather.ui.savedlocations

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.room.LocationEntity
import com.gerardbradshaw.whetherweather.ui.detail.DetailActivity
import com.gerardbradshaw.whetherweather.ui.detail.DetailPagerAdapter

class LocationListAdapter(private val context: Context):
    RecyclerView.Adapter<LocationListAdapter.LocationViewHolder>()
{
  private var listener: LocationClickedListener? = null
  private var locations: List<LocationEntity> = ArrayList()
  private val inflater = LayoutInflater.from(context)

  // ------------------------ ADAPTER FUNCTIONS ------------------------

  override fun getItemCount(): Int {
    return locations.size
  }
  
  fun setLocations(locations: List<LocationEntity>) {
    this.locations = locations
    notifyDataSetChanged()
  }


  // ------------------------ VIEW HOLDER ------------------------

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_saved_location, parent, false)
    return LocationViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
    try {
      holder.textView.text = locations[position].name
      holder.itemView.setOnClickListener { listener?.onLocationClicked(position) }

    } catch (e: IndexOutOfBoundsException) {
      Log.d(TAG, "onBindViewHolder: ERROR: index out of bounds for i = $position")
    }
  }

  class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.list_item_saved_location_text_view)
  }

  fun setLocationClickedListener(listener: LocationClickedListener) {
    this.listener = listener
  }

  interface LocationClickedListener {
    fun onLocationClicked(position: Int)
  }

  companion object {
    private const val TAG = "GGG LocationListAdapter"
  }
}