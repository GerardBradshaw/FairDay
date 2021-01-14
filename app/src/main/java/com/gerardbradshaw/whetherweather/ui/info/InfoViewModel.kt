package com.gerardbradshaw.whetherweather.ui.info

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.gerardbradshaw.whetherweather.BaseApplication
import com.gerardbradshaw.whetherweather.room.LocationEntity
import com.gerardbradshaw.whetherweather.room.Repository

class InfoViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: Repository = (application as BaseApplication).getRepository()
  val locationDataSet: List<LocationEntity>

  init {
    locationDataSet = repository.locations
  }

  fun insertLocationData(location: LocationEntity) {
    repository.insertLocationData(location)
  }

  fun deleteLocationData(location: LocationEntity) {
    repository.deleteLocationData(location)
  }
}