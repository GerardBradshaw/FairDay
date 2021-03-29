package com.gerardbradshaw.weatherview.children

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gerardbradshaw.weatherview.R
import com.gerardbradshaw.weatherview.datamodels.ForecastData
import kotlin.collections.ArrayList

internal class ForecastListAdapter(context: Context) :
  RecyclerView.Adapter<ForecastListAdapter.ForecastViewHolder>() {

  private lateinit var context: Context
  private val inflater = LayoutInflater.from(context)
  private var data = ArrayList<ForecastData>()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_forecast, parent, false)
    context = itemView.context
    return ForecastViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return data.size
  }

  fun setData(data: ArrayList<ForecastData>) {
    this.data = data
    notifyDataSetChanged()
  }

  override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
    val forecast = data[position]

    val url = context.getString(R.string.weather_view_condition_url_prefix) +
        forecast.conditionIconId + context.getString(R.string.weather_view_condition_url_suffix)

    Glide
      .with(context)
      .load(url)
      .into(holder.iconImageView)

    holder.iconImageView.contentDescription = context.getString(R.string.weather_view_cd_condition_description_prefix) +
        "${forecast.day}'s" + context.getString(R.string.weather_view_cd_condition_description_suffix)

    holder.dayTextView.text = if (position == 0) "Today" else forecast.day
    holder.minTempTextView.text = "${forecast.minTemp}"
    holder.maxTempTextView.text = "${forecast.maxTemp}"
  }

  class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val dayTextView: TextView = itemView.findViewById(R.id.forecast_day_name)
    val iconImageView: ImageView = itemView.findViewById(R.id.forecast_condition_icon)
    val minTempTextView: TextView = itemView.findViewById(R.id.forecast_min_temperature)
    val maxTempTextView: TextView = itemView.findViewById(R.id.forecast_max_temperature)
  }
}