package com.gerardbradshaw.whetherweather.activities.saved

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Component
import dagger.Subcomponent

@Subcomponent
interface SavedActivityComponent {

  @Subcomponent.Factory // For RT dependencies
  interface Factory {
    fun create(@BindsInstance context: Context,
               @BindsInstance activity: AppCompatActivity
    ): SavedActivityComponent
  }

  fun inject(savedActivity: SavedActivity)
}