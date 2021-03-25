package com.gerardbradshaw.weatherview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.widget.NestedScrollView
import com.gerardbradshaw.weatherview.WeatherViewUtils.getHourOnlyTimeString
import com.gerardbradshaw.weatherview.WeatherViewUtils.getTimeString
import com.gerardbradshaw.weatherview.datamodels.WeatherData
import com.gerardbradshaw.weatherview.subviews.detail.SmallDetailView
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class WeatherView : FrameLayout {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  private val progressBar: ProgressBar
  private val viewUi: NestedScrollView
  private val headlineView: WeatherHeadlineView
  private val hourlyGraphView: GraphView

  private val cloudinessView: SmallDetailView
  private val humidityView: SmallDetailView
  private val lastUpdateTime: TextView
  private val rainLastHourView: SmallDetailView
  private val rainLastThreeHourView: SmallDetailView
  private val sunriseView: SmallDetailView
  private val sunsetView: SmallDetailView
  private val windDirectionView: SmallDetailView
  private val windSpeedView: SmallDetailView

  init {
    val root = View.inflate(context, R.layout.view_weather_view, this)
    progressBar = root.findViewById(R.id.progress_bar)
    viewUi = root.findViewById(R.id.weather_view_ui)
    headlineView = root.findViewById(R.id.headline_view)
    hourlyGraphView = root.findViewById(R.id.hourly_graph_view)

    cloudinessView = root.findViewById(R.id.cloudiness_detail_view)
    humidityView = root.findViewById(R.id.humidity_detail_view)
    lastUpdateTime = root.findViewById(R.id.last_update_time_text_view)
    rainLastHourView = root.findViewById(R.id.rain_last_hour_detail_view)
    rainLastThreeHourView = root.findViewById(R.id.rain_last_three_hour_detail_view)
    sunriseView = root.findViewById(R.id.sunrise_detail_view)
    sunsetView = root.findViewById(R.id.sunset_detail_view)
    windDirectionView = root.findViewById(R.id.wind_direction_detail_view)
    windSpeedView = root.findViewById(R.id.wind_speed_detail_view)
  }

  fun setData(locality: String, weather: WeatherData?, isCurrentLocation: Boolean = false) {
    setIsUiVisible(false)

    val locationName =
      if (weather == null) context.getString(R.string.string_loading)
      else locality

    setHeadline(locationName, isCurrentLocation, weather)
    setHourlyGraph(weather)
    setWeeklyForecast(weather)
    setConditions(weather?.conditionName, weather?.conditionDescription, weather?.conditionIconId)
    setOtherInfo(weather)
    setLastUpdateTime(weather?.time)

    setIsUiVisible(true)
  }

  private fun setIsUiVisible(isVisible: Boolean) {
    if (isVisible) {
      viewUi.visibility = View.VISIBLE
      progressBar.visibility = View.GONE
    } else {
      viewUi.visibility = View.INVISIBLE
      progressBar.visibility = View.VISIBLE
    }
  }
  
  private fun setHeadline(location: String?, isCurrentLocation: Boolean = false, weather: WeatherData?) {
    headlineView.setLocation(location, isCurrentLocation)

    headlineView.setTemps(
      weather?.tempC?.roundToInt(),
      weather?.tempMinC?.roundToInt(),
      weather?.tempMaxC?.roundToInt())
  }
  
  private fun setHourlyGraph(weather: WeatherData?) {
    val hourlyData = weather?.hourlyData
    var minTemp = Double.MAX_VALUE
    var maxTemp = Double.MIN_VALUE

    if (hourlyData != null) {
      val iterator = hourlyData.iterator()

      val dataPoints = Array(12) {
        if (!iterator.hasNext()) {
          Log.e(TAG, "setHourlyDetails: error setting hourly data")
          hourlyGraphView.visibility = View.GONE
          return
        }

        val hourData = iterator.next()
        val time = hourData.time
        val temp = hourData.tempC?.toDouble()

        if (time == null || temp == null) {
          Log.e(TAG, "setHourlyDetails: error setting hourly data")
          hourlyGraphView.visibility = View.GONE
          return
        }

        minTemp = min(minTemp, temp)
        maxTemp = max(maxTemp, temp)
        DataPoint(it.toDouble(), temp)
      }

      with(hourlyGraphView) {
        visibility = View.INVISIBLE
        refreshDrawableState()

        viewport.setMinY(minTemp)
        viewport.setMaxY(maxTemp)
        viewport.isYAxisBoundsManual = true

        viewport.setMinX(dataPoints[0].x - 1)// - TimeUnit.HOURS.toMillis(1))
        viewport.setMaxX(dataPoints[dataPoints.size - 1].x + 1)// + TimeUnit.HOURS.toMillis(1))
        viewport.isXAxisBoundsManual = true

        gridLabelRenderer.gridColor = Color.LTGRAY
        gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
          override fun formatLabel(value: Double, isValueX: Boolean): String {
            val intValue = value.roundToInt()
            return when {
              isValueX && intValue in 0..11 -> {
                getHourOnlyTimeString(hourlyData[intValue].time!!)
              }
              isValueX -> "."
              else -> super.formatLabel(value, isValueX) + context.getString(R.string.symbol_degree)
            }
          }
        }
        gridLabelRenderer.verticalLabelsColor = Color.WHITE
        gridLabelRenderer.horizontalLabelsColor = Color.WHITE
        gridLabelRenderer.numHorizontalLabels = 6
        gridLabelRenderer.setHorizontalLabelsAngle(30)

        val series = LineGraphSeries(dataPoints)
        series.thickness = 10

        series.setOnDataPointTapListener { _, dataPoint ->
          if (dataPoint != null) {
            val time = hourlyData[dataPoint.x.roundToInt()].time!!
            val temp = dataPoint.y
            val text = "${temp.toInt()}" + context.getString(R.string.symbol_degree) + " at " +
                getHourOnlyTimeString(time)

            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
          }
        }

        addSeries(series)
        visibility = View.VISIBLE
      }
    }
  }

  private fun setWeeklyForecast(weather: WeatherData?) {
    // TODO
  }

  private fun setConditions(condition: String?, description: String?, conditionIconId: String?) {
    headlineView.setConditions(condition, description, conditionIconId)
  }

  private fun setOtherInfo(location: WeatherData?) {
    sunriseView.setInfo("Sunrise", getTimeString(location?.sunrise, location?.gmtOffset))
    sunsetView.setInfo("Sunset", getTimeString(location?.sunset, location?.gmtOffset))
    cloudinessView.setInfo("Cloudiness", "${location?.cloudiness}%")
    humidityView.setInfo("Humidity", "${location?.humidity ?: "-"}%")
    windSpeedView.setInfo("Wind speed", "${location?.windSpeed ?: "-"} m/s")
    windDirectionView.setInfo("Wind direction", meteorologicalToCardinal(location?.windDirection))
    rainLastHourView.setInfo("Rain last hr", "${location?.rainLastHour ?: "0"} mm")
    rainLastThreeHourView.setInfo("Rain last 3 hrs", "${location?.rainLastThreeHours ?: "0"} mm")
  }

  private fun setLastUpdateTime(time: Long?) {
    val lastUpdatedTimeText = "Last updated ${getTimeString(time, null)}"
    lastUpdateTime.text = lastUpdatedTimeText
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