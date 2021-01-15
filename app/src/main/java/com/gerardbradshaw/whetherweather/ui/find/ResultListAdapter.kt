package com.gerardbradshaw.whetherweather.ui.find

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.whetherweather.R

class ResultListAdapter(context: Context):
  RecyclerView.Adapter<ResultListAdapter.ResultViewHolder>() {
  
  private var results: List<String> = ArrayList()
  private val inflater = LayoutInflater.from(context)
  
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_location_result, parent, false)
    return ResultViewHolder(itemView)
  }
  
  override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
    try {
      val result = results[position]
      holder.textView.text = result
      
    } catch (e: IndexOutOfBoundsException) {
      Log.d(TAG, "onBindViewHolder: index out of bounds for i = $position")
    }
  }
  
  override fun getItemCount(): Int {
    return results.size
  }
  
  fun setResults(locations: List<String>) {
    this.results = locations
    notifyDataSetChanged()
  }
  
  companion object {
    private const val TAG = "ResultListAdapter"
  }
  
  class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.list_item_result_text_view)
  }
}