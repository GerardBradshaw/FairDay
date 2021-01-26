package com.gerardbradshaw.whetherweather.util.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

abstract class PermissionUtil {

  class RequestBuilder(
    private val permission: String,
    private val activity: AppCompatActivity
    ) {
    private var rationaleDialog: AlertDialog? = null
    private var dialogMessage = "This app requires permissions to deliver the best experience."
    private var dialogTitle = "Permissions required"
    private var dialogPositiveButtonText = "OK"
    private var dialogNegativeButtonText = "Not now"

    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null
    private var onPermissionIgnored: (() -> Unit)? = null

    private var requestPermission: ActivityResultLauncher<String?>? = null

    fun setRationaleDialogTitle(title: String): RequestBuilder {
      this.dialogTitle = title
      return this
    }

    fun setRationaleDialogMessage(message: String): RequestBuilder {
      this.dialogMessage = message
      return this
    }

    fun setRationaleDialogNegativeButtonText(s: String): RequestBuilder {
      this.dialogNegativeButtonText = s
      return this
    }

    fun setRationaleDialogPositiveButtonText(s: String): RequestBuilder {
      this.dialogPositiveButtonText = s
      return this
    }

    fun setOnPermissionGranted(onPermissionGranted: () -> Unit): RequestBuilder {
      this.onPermissionGranted = onPermissionGranted
      return this
    }

    fun setOnPermissionDenied(onPermissionDenied: () -> Unit): RequestBuilder {
      this.onPermissionDenied = onPermissionDenied
      return this
    }

    fun setOnPermissionIgnored(onPermissionIgnored: () -> Unit): RequestBuilder {
      this.onPermissionIgnored = onPermissionIgnored
      return this
    }

    fun setActivityResultLauncher(arl: ActivityResultLauncher<String?>): RequestBuilder {
      this.requestPermission = arl
      return this
    }

    fun buildAndRequest() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!isGranted(permission, activity)) {
          val shouldShowRationale = ActivityCompat
            .shouldShowRequestPermissionRationale(activity, permission)

          if (shouldShowRationale) showPermissionRationale()
          else showSystemPermissionsRequest()
        }
      }
    }

    private fun showPermissionRationale() {
      val dialog = rationaleDialog ?: AlertDialog.Builder(activity)
        .setMessage(dialogMessage)
        .setTitle(dialogTitle)
        .setPositiveButton(dialogPositiveButtonText) { _, _ -> showSystemPermissionsRequest() }
        .setNegativeButton(dialogNegativeButtonText) { _, _ -> }
        .create()

      dialog.show()
    }

    private fun showSystemPermissionsRequest() {
      if (requestPermission == null) {
        Log.d(TAG, "showSystemPermissionsRequest: ERROR: activity result launcher is null. Did " +
            "you forget to call setActivityResultLauncher() in the RequestBuilder?")
      }
      requestPermission?.launch(permission)
    }
  }



  companion object {
    private const val TAG = "GGG PermissionUtil"

    @JvmStatic
    fun isGranted(permission: String, context: Context): Boolean {
      val state = ContextCompat.checkSelfPermission(context, permission)
      return state == PackageManager.PERMISSION_GRANTED
    }
  }
}