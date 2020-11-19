package com.gerardbradshaw.whetherweather.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.gerardbradshaw.whetherweather.R

class WeatherView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val conditionImageView: ImageView
  private val locationTextView: TextView
  private val conditionsView: ConditionsView
  private val temperatureView: TemperatureView

  init {
    val root = View.inflate(context, R.layout.view_weather, this)

    conditionImageView = root.findViewById(R.id.condition_image_view)
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

  fun setConditions(condition: String?, description: String?, conditionIconId: String?) {
    val imageResId = getConditionImageUri(conditionIconId)

    Glide
      .with(context)
      .load(imageResId)
      .transition(DrawableTransitionOptions.withCrossFade())
      .into(conditionImageView)

    conditionsView.setConditions(condition, description, conditionIconId)
  }

  private fun getConditionImageUri(conditionIconId: String?): Int {
    if (conditionIconId == null|| conditionIconId.length < 3) return R.drawable.img_clear_day

    val number = Integer.parseInt(conditionIconId.substring(0,2))
    val isDay = conditionIconId.substring(3) == "d"

    return when (number) {
      2, 4 -> if (isDay) R.drawable.img_broken_and_few_clouds_day else R.drawable.img_broken_and_few_clouds_night
      3 -> if (isDay) R.drawable.img_scattered_clouds_day else R.drawable.img_scattered_clouds_night
      9 -> if (isDay) R.drawable.img_shower_day else R.drawable.img_shower_night
      10 -> R.drawable.img_rain_both
      11 -> R.drawable.img_storm_both
      13 -> if (isDay) R.drawable.img_snow_day else R.drawable.img_snow_night
      50 -> if (isDay) R.drawable.img_clear_day else R.drawable.img_clear_night
      else -> if (isDay) R.drawable.img_mist_day else R.drawable.img_mist_night
    }
  }
}