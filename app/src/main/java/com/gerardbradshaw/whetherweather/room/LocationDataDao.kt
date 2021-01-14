package com.gerardbradshaw.whetherweather.room

import androidx.room.*

@Dao
interface LocationDataDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertLocation(location: LocationEntity)

  @Delete
  fun deleteLocation(vararg location: LocationEntity)

  @Update
  fun updateLocation(location: LocationEntity)

  @Query("select * from weather_table")
  fun getLocationDataSet(): List<LocationEntity>
}