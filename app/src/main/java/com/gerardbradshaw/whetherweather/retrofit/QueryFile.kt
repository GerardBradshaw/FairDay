package com.gerardbradshaw.whetherweather.retrofit

import com.google.gson.annotations.SerializedName

class QueryFile {

  @SerializedName("q")
  var location: String? = null
    private set

  var appid: String? = null
    private set

  var units: String? = null
    private set

  var lang: String? = null
    private set
}