package com.github.matteobattilana.precipitationview.confetti

import android.graphics.Bitmap
import com.github.matteobattilana.precipitationview.PrecipitationType

/**
 * Created by Mitchell on 7/11/2017.
 *
 * This class stores the current precipitation type and it held by each Confetto.
 */
class ConfettoInfo(var precipitationType: PrecipitationType, var scaleFactor: Float, var customBitmap: Bitmap? = null)