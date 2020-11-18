package com.gerardbradshaw.whetherweather.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationDataDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertLocationData(locationData: LocationData)

  @Delete
  fun deleteLocation(vararg locationData: LocationData)

  @Update
  fun updateLocation(locationData: LocationData)

  @Query("select * from weather_table")
  fun getLocationDataSet(): LiveData<List<LocationData>>
}