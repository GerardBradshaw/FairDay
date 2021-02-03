package com.gerardbradshaw.whetherweather.util

object FileUtils {
  fun readTestResourceFile(filename: String): String {
    val fis = javaClass.classLoader?.getResourceAsStream(filename)
    return fis?.bufferedReader()?.readText() ?: ""
  }
}