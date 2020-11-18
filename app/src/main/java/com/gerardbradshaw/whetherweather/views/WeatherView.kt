package com.gerardbradshaw.whetherweather.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.gerardbradshaw.whetherweather.R

class WeatherView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private var locationTextView: TextView
  private var conditionsView: ConditionsView
  private var temperatureView: TemperatureView

  init {
    val root = View.inflate(context, R.layout.view_weather, this)

    locationTextView = root.findViewById(R.id.location_text_view)
    temperatureView = root.findViewById(R.id.temperature_view)
    conditionsView = root.findViewById(R.id.conditions_view)
  }

  fun setLocation(location: String?) {
    locationTextView.text = location ?: "Unknown location"
  }

  fun setTemperatures(current: Int?, min: Int?, max: Int?) {
    temperatureView.setTemps(current, min, max)
  }

  fun setConditions(condition: String?, description: String?) {
    conditionsView.setConditions(condition, description)
  }
}