package com.gerardbradshaw.whetherweather.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_table")
class LocationData(
  @PrimaryKey @ColumnInfo(name = "location") var location: String,
  @ColumnInfo(name = "time") var time: Int,
  @ColumnInfo(name = "condition") var condition: String,
  @ColumnInfo(name = "condition_icon_id") var conditionIconId: String,
  @ColumnInfo(name = "description") var description: String,
  @ColumnInfo(name = "temp") var temp: Int,
  @ColumnInfo(name = "min") var min: Int,
  @ColumnInfo(name = "max") var max: Int
) {}