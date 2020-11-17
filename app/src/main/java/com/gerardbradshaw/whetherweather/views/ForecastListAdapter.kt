package com.gerardbradshaw.whetherweather.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.util.ForecastData
import java.text.SimpleDateFormat
import java.util.*

class ForecastListAdapter(
  context: Context,
  private val data: List<ForecastData>
) : RecyclerView.Adapter<ForecastListAdapter.ForecastViewHolder>() {

  private val inflater = LayoutInflater.from(context)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_forecast, parent, false)
    return ForecastViewHolder(
      itemView
    )
  }

  override fun getItemCount(): Int {
    return data.size
  }

  override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
    val forecast = data[position]

    val sdf = SimpleDateFormat("h:mm a", Locale.US)
    holder.timeTextView.text = sdf.format(Date(forecast.time.times(1000L)))

    holder.conditionTextView.text = "☁️"
    holder.temperatureTextView.text = "${forecast.temperature}"
  }

  class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val timeTextView: TextView = itemView.findViewById(R.id.forecast_time_text_view)
    val conditionTextView: TextView = itemView.findViewById(R.id.forecast_condition_text_view)
    val temperatureTextView: TextView = itemView.findViewById(R.id.forecast_temperature_text_view)
  }
}