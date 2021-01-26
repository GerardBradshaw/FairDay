package com.gerardbradshaw.whetherweather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.gerardbradshaw.whetherweather.BaseApplication
import com.gerardbradshaw.whetherweather.room.LocationEntity
import com.gerardbradshaw.whetherweather.room.Repository

class BaseViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: Repository = (application as BaseApplication).getRepository()

  fun getAllLocations(): LiveData<List<LocationEntity>> {
    return repository.getLiveLocations()
  }

  fun saveLocation(location: LocationEntity) {
    repository.saveLocation(location)
  }

  fun deleteLocation(location: LocationEntity) {
    repository.deleteLocationData(location)
  }
}