package com.gerardbradshaw.weatherinfoview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.weatherinfoview.subviews.conditions.ConditionsView
import com.gerardbradshaw.weatherinfoview.subviews.detail.SmallDetailView
import com.gerardbradshaw.weatherinfoview.subviews.temperature.TemperatureView
import java.util.*

class WeatherInfoView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val locationTextView: TextView
  private val locationPinIcon: ImageView
  private val conditionsView: ConditionsView
  private val temperatureView: TemperatureView

  private val sunriseView: SmallDetailView
  private val sunsetView: SmallDetailView
  private val cloudinessView: SmallDetailView
  private val humidityView: SmallDetailView
  private val windSpeedView: SmallDetailView
  private val windDirectionView: SmallDetailView
  private val rainLastHourView: SmallDetailView
  private val rainLastThreeHourView: SmallDetailView
  private val lastUpdateTime: TextView

  init {
    val root = View.inflate(context, R.layout.view_info, this)
    locationTextView = root.findViewById(R.id.location_text_view)
    locationPinIcon = root.findViewById(R.id.location_pin_icon)
    temperatureView = root.findViewById(R.id.temperature_view)
    conditionsView = root.findViewById(R.id.conditions_view)
    sunriseView = root.findViewById(R.id.sunrise_detail_view)
    sunsetView = root.findViewById(R.id.sunset_detail_view)
    cloudinessView = root.findViewById(R.id.cloudiness_detail_view)
    humidityView = root.findViewById(R.id.humidity_detail_view)
    windSpeedView = root.findViewById(R.id.wind_speed_detail_view)
    windDirectionView = root.findViewById(R.id.wind_direction_detail_view)
    rainLastHourView = root.findViewById(R.id.rain_last_hour_detail_view)
    rainLastThreeHourView = root.findViewById(R.id.rain_last_three_hour_detail_view)
    lastUpdateTime = root.findViewById(R.id.last_update_time_text_view)
  }

  fun setLocation(location: WeatherData?, isCurrentLocation: Boolean = false) {
    if (location != null) {
      setName(location.name, isCurrentLocation)
      setTemperatures(location.currentTemp, location.minTemp, location.maxTemp)
      setConditions(location.condition, location.description, location.conditionIconId)
      setOtherInfo(location)
      setLastUpdateTime(location.timeUpdated)
    } else Log.d(TAG, "setLocation: ERROR: location is null")
  }

  fun setLocationName(name: String, isCurrentLocation: Boolean) {
    setName(name, isCurrentLocation)
  }

  private fun setName(location: String?, isCurrentLocation: Boolean = false) {
    locationTextView.text = location ?: "Unknown location"

    locationPinIcon.visibility =
      if (isCurrentLocation) View.VISIBLE
      else View.GONE
  }

  private fun setTemperatures(current: Int?, min: Int?, max: Int?) {
    temperatureView.setTemps(current, min, max)
  }

  private fun setConditions(condition: String?, description: String?, conditionIconId: String?) {
    conditionsView.setConditions(condition, description, conditionIconId)
  }

  private fun setForecast() {
    TODO()
  }

  private fun setOtherInfo(location: WeatherData) {
    sunriseView.setInfo("Sunrise", getTimeString(location.sunrise, location.gmtOffset))
    sunsetView.setInfo("Sunset", getTimeString(location.sunset, location.gmtOffset))
    cloudinessView.setInfo("Cloudiness", "${location.cloudiness}%")
    humidityView.setInfo("Humidity", "${location.humidity}%")
    windSpeedView.setInfo("Wind speed", "${location.windSpeed} m/s")
    windDirectionView.setInfo("Wind direction", meteorologicalToCardinal(location.windDirection))
    rainLastHourView.setInfo("Rain last hr", "${location.rainLastHour ?: "-"} mm")
    rainLastThreeHourView.setInfo("Rain last 3 hrs", "${location.rainLastThreeHours ?: "-"} mm")
  }

  private fun setLastUpdateTime(time: Long?) {
    val lastUpdatedTimeText = "Last updated ${getTimeString(time, null)}"
    lastUpdateTime.text = lastUpdatedTimeText
  }

  private fun getTimeString(time: Long?, gmtOffset: Long?): String {
    if (time == null) return "-"

    val cal = Calendar.getInstance()
    val localGmtOffset = cal.timeZone.getOffset(cal.timeInMillis)

    cal.timeInMillis = time + if (gmtOffset != null) gmtOffset - localGmtOffset else 0

    val minute = cal.get(Calendar.MINUTE)
    val minuteString = if (minute < 10) "0$minute" else "$minute"

    val amPm = if (cal.get(Calendar.AM_PM) == 0) "am" else "pm"

    return "${cal.get(Calendar.HOUR)}:$minuteString $amPm"
  }

  private fun meteorologicalToCardinal(m: Float?): String {
    return when {
      m == null || m < 0 -> "-"
      m <= 11.25f -> "N"
      m <= 33.75 -> "NNE"
      m <= 56.25 -> "NE"
      m <= 78.75 -> "ENE"
      m <= 101.25 -> "E"
      m <= 123.75 -> "ESE"
      m <= 146.25 -> "SE"
      m <= 168.75 -> "SSE"
      m <= 191.25 -> "S"
      m <= 213.75 -> "SSW"
      m <= 236.25 -> "SW"
      m <= 258.75 -> "WSW"
      m <= 281.25 -> "W"
      m <= 303.75 -> "WNW"
      m <= 326.25 -> "NW"
      m <= 348.75 -> "NNW"
      m <= 360 -> "N"
      else -> "-"
    }
  }

  companion object {
    private const val TAG = "WeatherView"
  }
}