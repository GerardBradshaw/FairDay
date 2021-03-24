package com.gerardbradshaw.fairday.retrofit

import com.google.gson.annotations.SerializedName

class OneCallWeatherFile {
  var lat: Float? = null
  var lon: Float? = null
  var timezone: String? = null
  @SerializedName("timezone_offset")  var timezoneOffset: Long? = null
  var current: Current? = null
  @SerializedName("minutely") var minutelyList: List<Minutely>? = null
  @SerializedName("hourly") var hourlyList: List<Hourly>? = null
  @SerializedName("daily") var dailyList: List<Daily>? = null
  @SerializedName("alerts") var alertsList: List<Alerts>? = null

  class Current {
    var dt: Long? = null
    var sunrise: Long? = null
    var sunset: Long? = null
    var temp: Float? = null
    @SerializedName("feels_like") var feelsLike: Float? = null
    var pressure: Float? = null
    var humidity: Float? = null
    @SerializedName("dew_point") var dewPoint: Float? = null
    var clouds: Float? = null
    var uvi: Float? = null
    var visibility: Int? = null
    @SerializedName("wind_speed") var windSpeed: Float? = null
    @SerializedName("wind_gust") var windGust: Float? = null
    @SerializedName("wind_deg") var windDirection: Float? = null
    var rain: Rain? = null
    var snow: Snow? = null
    @SerializedName("weather") var weatherList: List<Weather>? = null
  }

  class Hourly {
    var dt: Long? = null
    var temp: Float? = null
    @SerializedName("feels_like") var feelsLike: Float? = null
    var pressure: Float? = null
    var humidity: Float? = null
    @SerializedName("dew_point") var dewPoint: Float? = null
    var uvi: Float? = null
    var clouds: Float? = null
    var visibility: Int? = null
    @SerializedName("wind_speed") var windSpeed: Float? = null
    @SerializedName("wind_gust") var windGust: Float? = null
    @SerializedName("wind_deg") var windDirection: Float? = null
    @SerializedName("pop") var probabilityOfPrecipitation: Float? = null
    var rain: Rain? = null
    var snow: Snow? = null
    @SerializedName("weather") var weatherList: List<Weather>? = null
  }

  class Daily {
    var dt: Long? = null
    var sunrise: Long? = null
    var sunset: Long? = null
    var temp: Temp? = null
    @SerializedName("feels_like") var feelsLike: FeelsLike? = null
    var pressure: Float? = null
    var humidity: Float? = null
    @SerializedName("dew_point") var dewPoint: Float? = null
    @SerializedName("wind_speed") var windSpeed: Float? = null
    @SerializedName("wind_gust") var windGust: Float? = null
    @SerializedName("wind_deg") var windDirection: Float? = null
    var clouds: Float? = null
    var uvi: Float? = null
    @SerializedName("pop") var probabilityOfPrecipitation: Float? = null
    var rain: Float? = null
    var snow: Float? = null
    @SerializedName("weather") var weatherList: List<Weather>? = null
  }

  class Alerts {
    @SerializedName("sender_name") var senderName: String? = null
    var event: String? = null
    var start: Long? = null
    var end: Long? = null
    var description: String? = null
  }

  class Rain {
    @SerializedName("1h") var rainLastHour: Float? = null
  }

  class Snow {
    @SerializedName("1h") var snowLastHour: Float? = null
  }

  class Weather {
    var id: Int? = null
    var main: String? = null
    var description: String? = null
    var icon: String? = null
  }

  class Minutely {
    var dt: Long? = null
    var precipitation: Float? = null
  }

  class Temp {
    var morn: Float? = null
    var day: Float? = null
    var eve: Float? = null
    var night: Float? = null
    var min: Float? = null
    var max: Float? = null
  }

  class FeelsLike {
    var morn: Float? = null
    var day: Float? = null
    var eve: Float? = null
    var night: Float? = null
  }
}