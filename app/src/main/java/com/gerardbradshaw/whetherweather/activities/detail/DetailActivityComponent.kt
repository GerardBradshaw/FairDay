package com.gerardbradshaw.whetherweather.activities.detail

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface DetailActivityComponent {

  @Subcomponent.Factory // For RT dependencies
  interface Factory {
    fun create(@BindsInstance context: Context,
               @BindsInstance activity: AppCompatActivity
    ): DetailActivityComponent
  }

  fun inject(detailActivity: DetailActivity)
}