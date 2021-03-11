package com.gerardbradshaw.fairday.util

object FileUtils {
  fun readTextResourceFile(filename: String): String {
    val fis = javaClass.classLoader?.getResourceAsStream(filename)
    return fis?.bufferedReader()?.readText() ?: ""
  }
}