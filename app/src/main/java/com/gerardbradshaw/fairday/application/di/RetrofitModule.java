package com.gerardbradshaw.fairday.application.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class RetrofitModule {
  @Singleton
  @Provides
  Retrofit provideRetrofit(HttpUrl httpUrl) {
    return new Retrofit.Builder()
        .baseUrl(httpUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
  }
}
