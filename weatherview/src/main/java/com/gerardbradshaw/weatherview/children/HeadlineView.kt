package com.gerardbradshaw.weatherview.children

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.gerardbradshaw.weatherview.R

internal class HeadlineView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val locationNameTextView: TextView
  private val pinImageView: ImageView
  private val temperatureView: TemperatureView
  private val conditionView: ConditionsView

  init {
    View.inflate(context, R.layout.view_headline, this)

    locationNameTextView = findViewById(R.id.headline_location_text_view)
    pinImageView = findViewById(R.id.headline_location_pin_icon)
    temperatureView = findViewById(R.id.headline_temperature_view)
    conditionView = findViewById(R.id.headline_conditions_view)
  }

  fun setTemps(current: Int?, min: Int?, max: Int?) {
    temperatureView.setTemps(current, min, max)
  }

  fun setConditions(condition: String?, description: String?, conditionIconId: String?) {
    conditionView.setConditions(condition, description, conditionIconId)
  }

  fun setLocation(location: String?, isCurrentLocation: Boolean) {
    locationNameTextView.text = location ?: "Unknown location"

    pinImageView.visibility =
      if (isCurrentLocation) View.VISIBLE
      else View.GONE
  }
}