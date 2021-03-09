package com.gerardbradshaw.fairday.activities.detail.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.gerardbradshaw.fairday.R

class OpenWeatherCreditView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    View.inflate(context, R.layout.view_open_weather_credit, this)
  }
}