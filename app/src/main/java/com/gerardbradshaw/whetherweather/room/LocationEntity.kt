package com.gerardbradshaw.whetherweather.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
class LocationEntity(
  @PrimaryKey @ColumnInfo(name = "location") var locationName: String,
  @ColumnInfo(name = "utc_offset") var gmtOffset: Long,
  @ColumnInfo(name = "lat") var lat: Float,
  @ColumnInfo(name = "long") var lon: Float
  )