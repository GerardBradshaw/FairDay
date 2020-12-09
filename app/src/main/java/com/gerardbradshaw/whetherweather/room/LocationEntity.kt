package com.gerardbradshaw.whetherweather.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.gerardbradshaw.whetherweather.models.WeatherData
import java.util.*

@Entity(tableName = "weather_table")
class LocationEntity(
  @PrimaryKey @ColumnInfo(name = "location") var locationName: String,
  @ColumnInfo(name = "utc_offset") var gmtOffset: Long,
  @ColumnInfo(name = "lat") var lat: Float,
  @ColumnInfo(name = "long") var lon: Float
  ) {

  companion object {
    @Ignore
    @JvmStatic
    fun getEntityFromWeatherData(w: WeatherData): LocationEntity {
      val currentTime = Calendar.getInstance().timeInMillis

      return LocationEntity(
        w.locationName ?: "Unknown location",
        w.gmtOffset ?: 0L,
        w.latitude ?: 0f,
        w.longitude ?: 0f
      )
    }
  }
}