package com.gerardbradshaw.whetherweather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.gerardbradshaw.whetherweather.BaseApplication
import com.gerardbradshaw.whetherweather.room.LocationData
import com.gerardbradshaw.whetherweather.room.Repository

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: Repository = (application as BaseApplication).getRepository()
  val locationDataSet: LiveData<List<LocationData>>

  init {
    locationDataSet = repository.locationDataSet
  }

  fun insertLocationData(locationData: LocationData) {
    repository.insertLocationData(locationData)
  }

  fun deleteLocationData(locationData: LocationData) {
    repository.deleteLocationData(locationData)
  }
}