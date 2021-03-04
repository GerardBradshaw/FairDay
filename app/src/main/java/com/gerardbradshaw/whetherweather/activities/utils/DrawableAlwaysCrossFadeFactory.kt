package com.gerardbradshaw.whetherweather.activities.utils

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.TransitionFactory

class DrawableAlwaysCrossFadeFactory : TransitionFactory<Drawable> {
  private val resourceTransition: DrawableCrossFadeTransition =
    DrawableCrossFadeTransition(200, true)

  override fun build(dataSource: DataSource?, isFirstResource: Boolean): Transition<Drawable> {
    return resourceTransition
  }
}