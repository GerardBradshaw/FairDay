package com.gerardbradshaw.fairday.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.roundToInt

@Entity(tableName = "location_table")
class LocationEntity(
  @PrimaryKey @ColumnInfo(name = "locality") var locality: String,
  @ColumnInfo(name = "lat") var lat: Float,
  @ColumnInfo(name = "long") var lon: Float
  ) {

  override fun equals(other: Any?): Boolean {
    return (other is LocationEntity) &&
        (locality == other.locality) &&
        (other.lat.roundToInt() == lat.roundToInt()) &&
        (other.lon.roundToInt() == lon.roundToInt())
  }

  override fun hashCode(): Int {
    return locality.hashCode() + lat.roundToInt().hashCode() + lon.roundToInt().hashCode()
  }

  override fun toString(): String {
    return "[$locality, $lat, $lon]"
  }
}