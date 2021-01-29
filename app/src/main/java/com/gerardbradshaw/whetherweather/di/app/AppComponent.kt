package com.gerardbradshaw.whetherweather.di.app

import android.app.Application
import com.gerardbradshaw.whetherweather.di.activities.detail.DetailActivityComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [GlideModule::class, AppSubcomponents::class, CoroutineModule::class])
interface AppComponent {

  @Component.Builder
  interface Builder {
    fun setApplication(@BindsInstance application: Application): Builder
    fun build(): AppComponent
  }

  fun getDetailActivityComponentFactory(): DetailActivityComponent.Factory
}