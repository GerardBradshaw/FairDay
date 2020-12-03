package com.gerardbradshaw.whetherweather.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

abstract class PermissionUtil {

  class RequestBuilder(private val permission: String, private val activity: Activity) {

    private var dialogMessage = "This app requires permissions to deliver the best experience."
    private var dialogTitle = "Permissions required"
    private var dialogPositiveButtonText = "OK"
    private var dialogNegativeButton = "Not now"

    private var rationaleDialog: AlertDialog? = null

    fun overridePermissionRationaleDialog(rationaleDialog: AlertDialog): RequestBuilder {
      this.rationaleDialog = rationaleDialog
      return this
    }

    fun setPermissionRationaleDialogText(
      message: String,
      title: String,
      positiveButtonText: String?,
      negativeButtonText: String?
    ): RequestBuilder {
      dialogMessage = message
      dialogTitle = title
      dialogPositiveButtonText = positiveButtonText ?: dialogPositiveButtonText
      dialogNegativeButton = negativeButtonText ?: dialogNegativeButton
      return this
    }

    fun buildAndRequest() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!isPermissionGranted(permission, activity)) {
          val shouldShowRationale = ActivityCompat
            .shouldShowRequestPermissionRationale(activity, permission)

          if (shouldShowRationale) showPermissionRationale()
          else showSystemPermissionsRequest()
        }
      }
    }

    private fun isPermissionGranted(permission: String, activity: Activity): Boolean {
      val state = ContextCompat.checkSelfPermission(activity, permission)
      return state == PackageManager.PERMISSION_GRANTED
    }

    private fun showPermissionRationale() {
      val dialog = rationaleDialog ?: AlertDialog.Builder(activity)
        .setMessage(dialogMessage)
        .setTitle(dialogTitle)
        .setPositiveButton(dialogPositiveButtonText) { _, _ -> showSystemPermissionsRequest() }
        .setNegativeButton(dialogNegativeButton) { _, _ -> }
        .create()

      dialog.show()
    }

    private fun showSystemPermissionsRequest() {
      ActivityCompat.requestPermissions(
        activity,
        arrayOf(permission),
        PERMISSION_REQUEST_CODE)
    }
  }

  companion object {
    private const val TAG = "PermissionUtil"
    const val PERMISSION_REQUEST_CODE = 37

    @JvmStatic
    fun onRequestPermissionResultHelper(
      requestCode: Int,
      grantResults: IntArray,
      onPermissionGranted: () -> (Unit),
      onPermissionIgnored: (() -> (Unit))? = null,
      onPermissionDenied: (() -> (Unit))? = null
    ) {
      if (requestCode != PERMISSION_REQUEST_CODE) return

      when {
        grantResults.isEmpty() -> {
          onPermissionIgnored?.invoke()
          Log.d(TAG, "onRequestPermissionsResult: user cancelled")
        }
        grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
          Log.d(TAG, "onRequestPermissionsResult: permission granted")
          onPermissionGranted()
        }
        else -> {
          onPermissionDenied?.invoke()
          Log.d(TAG, "onRequestPermissionsResult: user denied permission")
        }
      }
    }

    @JvmStatic
    fun isPermissionGranted(permission: String, context: Context): Boolean {
      val state = ContextCompat.checkSelfPermission(context, permission)
      return state == PackageManager.PERMISSION_GRANTED
    }
  }
}