package com.gerardbradshaw.fairday.widget

import android.content.Context
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface WidgetComponent {
  @Subcomponent.Factory // For RT dependencies
  interface Factory {
    fun create(@BindsInstance context: Context): WidgetComponent
  }

  fun inject(widgetProvider: FairDayWidgetProvider)
}