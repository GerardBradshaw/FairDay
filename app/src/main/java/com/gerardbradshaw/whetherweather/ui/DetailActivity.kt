package com.gerardbradshaw.whetherweather.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.gerardbradshaw.whetherweather.BuildConfig
import com.gerardbradshaw.whetherweather.R
import com.gerardbradshaw.whetherweather.retrofit.JsonPlaceholderApi
import com.gerardbradshaw.whetherweather.retrofit.WeatherFile
import com.gerardbradshaw.whetherweather.util.WeatherData
import com.gerardbradshaw.whetherweather.views.ConditionsView
import com.gerardbradshaw.whetherweather.views.TemperatureView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.HashMap

class DetailActivity : AppCompatActivity(), View.OnClickListener {
  private lateinit var locationTextView: TextView
  private lateinit var conditionsView: ConditionsView
  private lateinit var temperatureView: TemperatureView

  lateinit var jsonPlaceholderApi: JsonPlaceholderApi


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_detail)
    locationTextView = findViewById(R.id.location_text_view)
    temperatureView = findViewById(R.id.temperature_view)
    conditionsView = findViewById(R.id.conditions_view)

    supportActionBar?.setDisplayShowTitleEnabled(false)

    initRetrofit()
    retrieveData()
  }

  private fun initRetrofit() {
    val retrofit = Retrofit.Builder()
      .baseUrl("https://api.openweathermap.org/data/2.5/")
      .addConverterFactory(GsonConverterFactory.create())
      .build()

    jsonPlaceholderApi = retrofit.create(JsonPlaceholderApi::class.java)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_action_bar_detail_activity, menu)
    return super.onCreateOptionsMenu(menu)
  }

  private fun retrieveData() {
    val params = HashMap<String, String>()
    params["q"] = "San Francisco"
    params["appId"] = ""

    val call = jsonPlaceholderApi.getWeather(params)

    call.enqueue(object : Callback<WeatherFile> {
      override fun onFailure(call: Call<WeatherFile>, t: Throwable) {
        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
      }

      override fun onResponse(call: Call<WeatherFile>, response: Response<WeatherFile>) {
        if (!response.isSuccessful) {
          val msg = "Code: ${response.code()}"
          Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
          return
        }

        val data = response.body()

        if (data == null) Toast.makeText(applicationContext, getString(R.string.message_no_data), Toast.LENGTH_LONG).show()
        else {
          val weatherData = WeatherData(data)
          displayData(weatherData)
        }
      }
    })
  }

  private fun displayData(data: WeatherData) {
    locationTextView.text = "${data.location}"
    conditionsView.setConditions(getString(R.string.cloud_emoji), data.description!!)
    temperatureView.setTemps(data.temp, data.min, data.max)

//    val strb = StringBuilder()
//    val sdf = SimpleDateFormat("h:mm a", Locale.US)
//
//    strb.append("Humidity ${data.humidity}%\n")
//    strb.append("Wind speed: ${data.windSpeed} m/s\n")
//    strb.append("Wind direction: ${data.windDirection}°\n")
//    strb.append("Cloudiness ${data.cloudiness}%\n")
//    strb.append("Rain last hour: ${data.rainLastHour} mm\n")
//    strb.append("Rain last 3 hours: ${data.rainLastThreeHours} mm\n")
//    strb.append("Updated: ${sdf.format(Date(data.timeUpdated!!.times(1000L)))}\n")
//    strb.append("Sunset: ${sdf.format(Date(data.sunset!!.times(1000L)))}\n")
//    strb.append("Sunrise: ${sdf.format(Date(data.sunrise!!.times(1000L)))}\n")
  }

  override fun onClick(v: View?) {
    if (v == null) return

    when (v.id) {
      else -> return
    }
  }

  companion object {
    private const val OPEN_WEATHER_APP_KEY = BuildConfig.OPEN_WEATHER_APP_KEY
  }
}