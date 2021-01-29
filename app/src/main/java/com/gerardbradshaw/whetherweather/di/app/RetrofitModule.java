package com.gerardbradshaw.whetherweather.di.app;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public abstract class RetrofitModule {
  private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
  
  @Singleton
  @Provides
  static Retrofit provideRetrofit() {
    return new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
  }
}
