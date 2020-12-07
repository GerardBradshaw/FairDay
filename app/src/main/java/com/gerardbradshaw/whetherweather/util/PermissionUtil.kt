package com.gerardbradshaw.whetherweather.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

abstract class PermissionUtil {

  class RequestBuilder(private val permission: String, private val activity: AppCompatActivity) {

    private var dialogMessage = "This app requires permissions to deliver the best experience."
    private var dialogTitle = "Permissions required"
    private var dialogPositiveButtonText = "OK"
    private var dialogNegativeButtonText = "Not now"

    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null
    private var onPermissionIgnored: (() -> Unit)? = null
    private var requestPermission: ActivityResultLauncher<String?>? = null


    private var rationaleDialog: AlertDialog? = null

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
        .setNegativeButton(dialogNegativeButtonText) { _, _ -> }
        .create()

      dialog.show()
    }

    private fun showSystemPermissionsRequest() {
      if (requestPermission == null) {
        Log.d(TAG, "showSystemPermissionsRequest: activity result launcher is null. Did you forget to call setActivityResultLauncher() in the RequestBuilder?")
      }

      requestPermission?.launch(permission)

//      ActivityCompat.requestPermissions(
//        activity,
//        arrayOf(permission),
//        PERMISSION_REQUEST_CODE)
    }

//    private val requestPermission =
//      activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
//        onPermissionRequestResult(it)
//      }

//    private fun onPermissionRequestResult(isGranted: Boolean?) {
//      when (isGranted) {
//        true -> {
//          Log.d(TAG, "onRequestPermissionsResult: user granted permission")
//
//          if (onPermissionGranted != null) onPermissionGranted?.invoke()
//          else Log.d(TAG, "onPermissionRequestResult: ::onPermissionGranted not set. Did you forget to set it in the request builder?")
//        }
//
//        false -> {
//          Log.d(TAG, "onRequestPermissionsResult: user denied permission")
//
//          if (onPermissionDenied != null) onPermissionDenied?.invoke()
//          else Log.d(TAG, "onPermissionRequestResult: ::onPermissionDenied not set. Did you forget to set it in the request builder?")
//        }
//
//        else -> {
//          Log.d(TAG, "onRequestPermissionsResult: user ignored permission")
//
//          if (onPermissionIgnored != null) onPermissionIgnored?.invoke()
//          else Log.d(TAG, "onPermissionRequestResult: ::onPermissionIgnored not set. Did you forget to set it in the request builder?")
//        }
//      }
//    }
  }

  companion object {
    private const val TAG = "PermissionUtil"
    const val PERMISSION_REQUEST_CODE = 37

//    @JvmStatic
//    fun onRequestPermissionResultHelper(
//      requestCode: Int,
//      grantResults: IntArray,
//      onPermissionGranted: () -> (Unit),
//      onPermissionIgnored: (() -> (Unit))? = null,
//      onPermissionDenied: (() -> (Unit))? = null
//    ) {
//      if (requestCode != PERMISSION_REQUEST_CODE) return
//
//      when {
//        grantResults.isEmpty() -> {
//          onPermissionIgnored?.invoke()
//          Log.d(TAG, "onRequestPermissionsResult: user cancelled")
//        }
//        grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
//          Log.d(TAG, "onRequestPermissionsResult: permission granted")
//          onPermissionGranted()
//        }
//        else -> {
//          onPermissionDenied?.invoke()
//          Log.d(TAG, "onRequestPermissionsResult: user denied permission")
//        }
//      }
//    }

    @JvmStatic
    fun checkIsGranted(permission: String, context: Context): Boolean {
      val state = ContextCompat.checkSelfPermission(context, permission)
      return state == PackageManager.PERMISSION_GRANTED
    }
  }
}