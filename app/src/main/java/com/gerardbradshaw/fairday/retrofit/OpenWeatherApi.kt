package com.gerardbradshaw.fairday.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface OpenWeatherApi {
  @Deprecated("Use getOneCallWeather() instead.")
  @GET("weather")
  fun getWeather(@QueryMap params: Map<String, String>): Call<WeatherFile>

  @GET("onecall")
  fun getOneCallWeather(@QueryMap params: Map<String, String>): Call<OneCallWeatherFile>
}