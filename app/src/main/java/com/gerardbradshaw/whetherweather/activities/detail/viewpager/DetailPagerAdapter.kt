package com.gerardbradshaw.whetherweather.activities.detail.viewpager

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.weatherinfoview.WeatherInfoView
import com.gerardbradshaw.whetherweather.R
import java.util.*
import javax.inject.Inject
import kotlin.IndexOutOfBoundsException
import kotlin.collections.ArrayList

class DetailPagerAdapter @Inject constructor(private val context: Context) :
    RecyclerView.Adapter<DetailPagerAdapter.DetailViewHolder>()
{
  private val inflater = LayoutInflater.from(context)
  private var data: ArrayList<DetailPagerItem> = ArrayList()
  private var listener: DataChangeListener? = null


  // ------------------------ ADAPTER FUNCTIONS ------------------------

  override fun getItemCount(): Int {
    return data.size
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
    val itemView = inflater.inflate(R.layout.list_item_weather_detail, parent, false)
    return DetailViewHolder(itemView)
  }

  override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
    try {
      val itemData = data[position]
      val locality = data[position].locationEntity.locality
      val isCurrentLocation = itemData.isCurrentLocation

      holder.weatherView.setData(locality, itemData.weatherData, isCurrentLocation)

    } catch (e: IndexOutOfBoundsException) {
      Log.e(TAG, "onBindViewHolder: invalid position $position", e)
    }
  }

  fun setItemCountChangeListener(listener: DataChangeListener) {
    this.listener = listener
  }

  fun setData(data: ArrayList<DetailPagerItem>) {
    val diffResult = DiffUtil.calculateDiff(MyDiffCallback(this.data, data))
    this.data = data
    diffResult.dispatchUpdatesTo(this)
    listener?.onDataUpdate()
  }

  fun getConditionIdFor(position: Int): String? {
    return getWeatherDataFor(position)?.conditionIconId
  }

  private fun getWeatherDataFor(position: Int): WeatherData? {
    return try {
      data[position].weatherData
    } catch (e: IndexOutOfBoundsException) {
      Log.e(TAG, "getWeatherDataFor: invalid index $position")
      null
    }
  }

  class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val weatherView: WeatherInfoView = itemView.findViewById(R.id.list_item_weather_view)
  }

  private class MyDiffCallback(
    private val oldList: List<DetailPagerItem>,
    private val newList: List<DetailPagerItem>
  ) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
      oldList[oldItemPosition] == newList[newItemPosition]

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
      oldList[oldItemPosition] == newList[newItemPosition]
  }

  interface DataChangeListener {
    fun onDataUpdate()
  }

  companion object {
    private const val TAG = "GGG DetailPagerAdapter"
  }
}