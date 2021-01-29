package com.gerardbradshaw.whetherweather.di.activities.detail

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.gerardbradshaw.whetherweather.ui.detail.DetailActivity
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