package com.gerardbradshaw.fairday.activities.saved

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.fairday.R
import com.gerardbradshaw.fairday.room.LocationEntity

class LocationListAdapter(private val context: Context):
    RecyclerView.Adapter<LocationListAdapter.LocationViewHolder>()
{
  private var listener: LocationClickedListener? = null
  private var locations: List<LocationEntity> = ArrayList()
  private val inflater = LayoutInflater.from(context)

  override fun getItemCount(): Int {
    return locations.size
  }
  
  fun setLocations(locations: List<LocationEntity>) {
    this.locations = locations
    notifyDataSetChanged()
  }

  fun setLocationClickedListener(listener: LocationClickedListener) {
    this.listener = listener
  }

  fun getEntityAtPosition(position: Int): LocationEntity? {
    return try {
      locations[position]
    } catch (e: java.lang.IndexOutOfBoundsException) {
      Log.e(TAG, "getEntityAtPosition: invalid position $position", e)
      null
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_saved_location, parent, false)
    return LocationViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
    try {
      holder.textView.text = locations[position].locality
      holder.itemView.setOnClickListener { listener?.onLocationClicked(position) }

    } catch (e: IndexOutOfBoundsException) {
      Log.e(TAG, "onBindViewHolder: index out of bounds for i = $position", e)
    }
  }

  class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.list_item_saved_location_text_view)
  }



  interface LocationClickedListener {
    fun onLocationClicked(position: Int)
  }

  companion object {
    private const val TAG = "GGG LocationListAdapter"
  }
}