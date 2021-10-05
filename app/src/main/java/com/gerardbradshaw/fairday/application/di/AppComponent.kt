package com.gerardbradshaw.fairday.application.di

import android.app.Application
import com.gerardbradshaw.fairday.activities.detail.DetailActivityComponent
import com.gerardbradshaw.fairday.activities.saved.SavedActivityComponent
import com.gerardbradshaw.fairday.application.BaseApplication
import com.gerardbradshaw.fairday.application.annotations.IsTest
import dagger.BindsInstance
import dagger.Component
import okhttp3.HttpUrl
import javax.inject.Singleton

@Singleton
@Component(modules = [GlideModule::class, RetrofitModule::class, AppSubcomponents::class])
interface AppComponent {

  @Component.Builder
  interface Builder {
    fun setApplication(@BindsInstance application: Application): Builder
    fun setIsTest(@BindsInstance @IsTest isTest: Boolean): Builder
    fun setHttpUrl(@BindsInstance httpUrl: HttpUrl): Builder
    fun build(): AppComponent
  }

  fun getDetailActivityComponentFactory(): DetailActivityComponent.Factory
  fun getSavedActivityComponentFactory(): SavedActivityComponent.Factory
//  fun getWidgetComponentFactory(): WidgetComponent.Factory
  fun inject(baseApplication: BaseApplication)
}