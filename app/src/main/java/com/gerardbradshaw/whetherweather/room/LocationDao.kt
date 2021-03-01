package com.gerardbradshaw.whetherweather.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface LocationDao {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  fun insertLocation(location: LocationEntity)

  @Delete
  fun deleteLocation(vararg location: LocationEntity)

  @Update
  fun updateLocation(location: LocationEntity)

  @Query("select * from location_table")
  fun getLocationDataSet(): List<LocationEntity>

  @Query("select * from location_table")
  fun getLiveLocations(): LiveData<List<LocationEntity>>

  @Query("select * from location_table")
  fun getLocations(): List<LocationEntity>

  @Query("delete from location_table")
  fun deleteAll()
}