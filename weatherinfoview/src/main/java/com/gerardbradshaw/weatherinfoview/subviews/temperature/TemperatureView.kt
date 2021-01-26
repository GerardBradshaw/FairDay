package com.gerardbradshaw.weatherinfoview.subviews.temperature

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.gerardbradshaw.weatherinfoview.R

class TemperatureView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val currentTempTextView: TextView
  private val maxTempTextView: TextView
  private val minTempTextView: TextView

  fun setTemps(current: Int?, min: Int?, max: Int?) {
    currentTempTextView.text = "$current"
    minTempTextView.text = "$min"
    maxTempTextView.text = "$max"
  }

  fun setCurrentTemp(current: Int) {
    currentTempTextView.text = "$current"
  }

  fun setMinTemp(min: Int) {
    minTempTextView.text = "$min"
  }

  fun setMaxTemp(max: Int) {
    maxTempTextView.text = "$max"
  }

  init {
    View.inflate(context, R.layout.view_temp_info, this)

    currentTempTextView = findViewById(R.id.current_text_view)
    maxTempTextView = findViewById(R.id.max_text_view)
    minTempTextView = findViewById(R.id.min_text_view)
  }
}