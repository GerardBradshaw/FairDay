package com.gerardbradshaw.whetherweather.ui.weather

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.gerardbradshaw.whetherweather.BaseApplication
import com.gerardbradshaw.whetherweather.room.LocationEntity
import com.gerardbradshaw.whetherweather.room.Repository

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: Repository = (application as BaseApplication).getRepository()
  val locationDataSet: List<LocationEntity>

  init {
    locationDataSet = repository.locations
  }

  fun insertWeatherData(locationData: LocationEntity) {
    repository.insertLocationData(locationData)
  }

  fun deleteLocationData(locationData: LocationEntity) {
    repository.deleteLocationData(locationData)
  }
}