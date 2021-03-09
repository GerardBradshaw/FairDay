package com.gerardbradshaw.fairday.retrofit

import com.google.gson.annotations.SerializedName

class WeatherFile {
  @SerializedName("coord") var coordinates: Coordinates? = null
  var weather: List<Weather>? = null
  var base: String? = null
  var main: MainTemps? = null
  var visibility: Int? = null
  var wind: Wind? = null
  var clouds: Clouds? = null
  var rain: Rain? = null
  var snow: Snow? = null
  @SerializedName("dt") var updateTime: Long? = null
  var sys: Sys? = null
  @SerializedName("timezone") var gmtOffset: Long? = null
  var id: Int? = null
  var name: String? = null
  var cod: Int? = null

  class Coordinates {
    var lon: Float? = null
    var lat: Float? = null
  }

  class Weather {
    var id: Int? = null
    var main: String? = null
    var description: String? = null
    var icon: String? = null
  }

  class MainTemps {
    var temp: Float? = null
    @SerializedName("feels_like") var feelsLike: Float? = null
    var pressure: Float? = null
    var humidity: Float? = null
    @SerializedName("temp_min") var tempMin: Float? = null
    @SerializedName("temp_max") var tempMax: Float? = null
    @SerializedName("sea_level") var seaLevel: Float? = null
    @SerializedName("grnd_level") var groundLevel: Float? = null
  }

  class Wind {
    var speed: Float? = null
    var deg: Float? = null
    var gust: Float? = null
  }

  class Clouds {
    var all: Float? = null
  }

  class Rain {
    @SerializedName("1h") var mmLastHour: Float? = null
    @SerializedName("3h") var mmLastThreeHours: Float? = null
  }

  class Snow {
    @SerializedName("1h") var mmLastHour: Float? = null
    @SerializedName("3h") var mmLastThreeHours: Float? = null
  }

  class Sys {
    var type: Int? = null
    var id: Int? = null
    var message: Float? = null
    var country: String? = null
    var sunrise: Long? = null
    var sunset: Long? = null
  }
}
