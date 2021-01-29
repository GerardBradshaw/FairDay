package com.gerardbradshaw.whetherweather.di.app

import android.app.Application
import com.gerardbradshaw.whetherweather.BaseApplication
import com.gerardbradshaw.whetherweather.di.activities.detail.DetailActivityComponent
import com.gerardbradshaw.whetherweather.di.annotations.ThreadDefault
import com.gerardbradshaw.whetherweather.di.annotations.ThreadIo
import com.gerardbradshaw.whetherweather.di.annotations.ThreadMain
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Singleton
@Component(modules = [GlideModule::class, RetrofitModule::class, AppSubcomponents::class])
interface AppComponent {

  @Component.Builder
  interface Builder {
    fun setApplication(
      @BindsInstance application: Application
    ): Builder

    fun setMainDispatcher(
      @BindsInstance @ThreadMain dispatcher: CoroutineDispatcher
    ): Builder

    fun setDefaultDispatcher(
      @BindsInstance @ThreadDefault dispatcher: CoroutineDispatcher
    ): Builder

    fun setIoDispatcher(
      @BindsInstance @ThreadIo dispatcher: CoroutineDispatcher
    ): Builder

    fun build(): AppComponent
  }

  fun getDetailActivityComponentFactory(): DetailActivityComponent.Factory
  fun inject(baseApplication: BaseApplication)
}