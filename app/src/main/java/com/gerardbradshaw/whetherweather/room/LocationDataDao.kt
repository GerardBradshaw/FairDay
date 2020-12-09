package com.gerardbradshaw.whetherweather.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationDataDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertLocation(locationData: LocationEntity)

  @Delete
  fun deleteLocation(vararg locationData: LocationEntity)

  @Update
  fun updateLocation(locationData: LocationEntity)

  @Query("select * from weather_table")
  fun getLocationDataSet(): List<LocationEntity>
}