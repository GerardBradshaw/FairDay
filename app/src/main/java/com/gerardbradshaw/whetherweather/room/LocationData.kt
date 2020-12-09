package com.gerardbradshaw.whetherweather.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.gerardbradshaw.whetherweather.models.WeatherData
import java.util.*

@Entity(tableName = "weather_table")
class LocationData(
  @PrimaryKey @ColumnInfo(name = "location") var locationName: String,
  @ColumnInfo(name = "time") var timeUpdated: Long,
  @ColumnInfo(name = "condition") var condition: String,
  @ColumnInfo(name = "condition_icon_id") var conditionIconId: String,
  @ColumnInfo(name = "description") var description: String,
  @ColumnInfo(name = "temp") var currentTemp: Int,
  @ColumnInfo(name = "min") var minTemp: Int,
  @ColumnInfo(name = "max") var maxTemp: Int,
  @ColumnInfo(name = "humidity") var humidity: Float,
  @ColumnInfo(name = "wind_speed") var windSpeed: Float,
  @ColumnInfo(name = "wind_direction") var windDirection: Float,
  @ColumnInfo(name = "rain_last_hour") var rainLastHour: Float,
  @ColumnInfo(name = "rain_last_three_hour") var rainLastThreeHours: Float,
  @ColumnInfo(name = "cloudiness") var cloudiness: Float,
  @ColumnInfo(name = "sunrise") var sunrise: Long,
  @ColumnInfo(name = "sunset") var sunset: Long,
  @ColumnInfo(name = "utc_offset") var gmtOffset: Long,
  @ColumnInfo(name = "lat") var lat: Float,
  @ColumnInfo(name = "long") var lon: Float
  ) {

  companion object {
    @Ignore
    @JvmStatic
    fun getLocationFromWeatherData(w: WeatherData): LocationData {
      val currentTime = Calendar.getInstance().timeInMillis

      return LocationData(
        w.locationName ?: "Unknown location",
        w.timeUpdated ?: currentTime,
        w.condition ?: "",
        w.conditionIconId ?: "",
        w.description ?: "",
        w.currentTemp ?: -273,
        w.minTemp ?: -273,
        w.maxTemp ?: -273,
        w.humidity ?: 0f,
        w.windSpeed ?: 0f,
        w.windDirection ?: 0f,
        w.rainLastHour ?: 0f,
        w.rainLastThreeHours ?: 0f,
        w.cloudiness ?: 0f,
        w.sunrise ?: currentTime,
        w.sunset ?: currentTime,
        w.gmtOffset ?: 0L,
        w.latitude ?: 0f,
        w.longitude ?: 0f
      )
    }
  }
}