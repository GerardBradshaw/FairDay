package com.github.matteobattilana.precipitationview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.FrameLayout
import com.github.jinatonic.confetti.ConfettiManager
import com.github.matteobattilana.precipitationview.confetti.ConfettoInfo
import com.github.matteobattilana.precipitationview.confetti.MutableRectSource
import com.github.matteobattilana.precipitationview.confetti.WeatherConfettoGenerator

/**
 * Created by Mitchell Skaggs on 7/4/2017.
 *
 *
 * This facilitates interacting with a [ConfettiManager].
 */

class PrecipitationView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
  val confettiSource: MutableRectSource
  val confettiManager: ConfettiManager

  var angle: Int = 0
    set(value) {
      field = value
      angleRadians = Math.toRadians(value.toDouble())
      updateVelocities()
    }

  var scaleFactor: Float = 1.0f
    set(value) {
      field = value
      confettoInfo.scaleFactor = value;
    }

  var angleRadians: Double = 0.0
    private set

  var speed: Int = 0
    set(value) {
      field = value
      updateVelocities()
    }

  var fadeOutPercent: Float = 1f

  var emissionRate: Float = 0f
    set(value) {
      field = value
      updateEmissionRate()
    }

  var precipitationType: PrecipitationType = PrecipitationType.CLEAR
    set(value) {
      field = value
      confettoInfo.precipitationType = value
    }

  fun setCustomBitmap(bitmap: Bitmap) {
    confettoInfo.precipitationType = PrecipitationType.CUSTOM
    confettoInfo.customBitmap = bitmap
  }

  private val confettoInfo = ConfettoInfo(PrecipitationType.CLEAR, 1.0f)

  init {
    confettiSource = MutableRectSource(0, 0)
    val confettoGenerator = WeatherConfettoGenerator(confettoInfo)
    confettiManager = ConfettiManager(context, confettoGenerator, confettiSource, this)
      .setEmissionDuration(ConfettiManager.INFINITE_DURATION)
      .enableFadeOut { input -> (fadeOutPercent - input).coerceIn(0f, 1f) }
      .animate()
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    super.onLayout(changed, left, top, right, bottom)

    setConfettiBoundsToSelf()
  }

  fun setWeatherData(weatherData: WeatherData) {
    precipitationType = weatherData.precipitationType
    emissionRate = weatherData.emissionRate
    speed = weatherData.speed
    resetWeather()
  }

  fun resetWeather() {
    confettiManager.animate()
  }

  private fun setConfettiBoundsToSelf() {
    val offscreenSpawnDistance = Math.tan(angleRadians).coerceIn(-5.0, 5.0) * height // Coerce to prevent asymptotes of the tan() function breaking things

    confettiManager.setBound(Rect(0, 0, width, height))
    confettiSource.setBounds((-offscreenSpawnDistance).toInt().coerceAtMost(0), 0, (width - offscreenSpawnDistance).toInt().coerceAtLeast(width), 0)
  }

  private fun updateEmissionRate() {
    confettiManager.setEmissionRate(emissionRate)
  }

  private fun updateVelocities() {
    val yVelocity = Math.cos(angleRadians).toFloat() * speed
    val xVelocity = Math.sin(angleRadians).toFloat() * speed

    confettiManager
      .setVelocityY(yVelocity, yVelocity * .05F)
      .setVelocityX(xVelocity, xVelocity * .05F)
      .setInitialRotation(-angle)

    setConfettiBoundsToSelf()
  }
}
