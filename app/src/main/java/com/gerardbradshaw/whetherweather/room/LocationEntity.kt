package com.gerardbradshaw.whetherweather.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
class LocationEntity(
    @PrimaryKey @ColumnInfo(name = "location") var name: String,
    @ColumnInfo(name = "utc_offset") var gmtOffset: Long,
    @ColumnInfo(name = "lat") var lat: Float,
    @ColumnInfo(name = "long") var lon: Float
  ) {

  override fun equals(other: Any?): Boolean {
    return other is LocationEntity && name == other.name
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }

  override fun toString(): String {
    return "[$name, $gmtOffset, $lat, $lon]"
  }
}