package com.gerardbradshaw.weatherview.children

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.weatherview.R
import com.gerardbradshaw.weatherview.datamodels.ForecastData

internal class ForecastView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val recyclerView: RecyclerView
  private val adapter: ForecastListAdapter

  init {
    val root = View.inflate(context, R.layout.view_forecast, this)
    recyclerView = root.findViewById(R.id.forecast_recycler)
    adapter = ForecastListAdapter(context)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
  }

  fun setData(data: ArrayList<ForecastData>) {
    adapter.setData(data)
  }
}