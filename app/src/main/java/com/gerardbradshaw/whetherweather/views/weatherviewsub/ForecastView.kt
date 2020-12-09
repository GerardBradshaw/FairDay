package com.gerardbradshaw.whetherweather.views.weatherviewsub

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.models.ForecastData

class ForecastView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val recyclerView: RecyclerView

  init {
    View.inflate(context, R.layout.view_forecast, this)

    recyclerView = findViewById(R.id.forecast_recycler)
  }

  fun setForecastData(data: List<ForecastData>) {
    val adapter = ForecastListAdapter(context, data)

    recyclerView.adapter = adapter

    recyclerView.layoutManager =
      LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
  }
}