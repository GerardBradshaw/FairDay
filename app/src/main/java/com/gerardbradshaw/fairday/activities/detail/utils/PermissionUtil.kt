package com.gerardbradshaw.fairday.activities.detail.utils

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

  class RationaleDialogBuilder(
    private val permission: String,
    private val activity: AppCompatActivity
    ) {
    private var rationaleDialog: AlertDialog? = null
    private var dialogMessage = "This app requires permissions to deliver the best experience."
    private var dialogTitle = "Permission required"
    private var acceptedButtonText = "OK"
    private var dismissedButtonText = "Not now"

    private var onAccepted: (() -> Unit)? = null
    private var onDismissed: (() -> Unit)? = null
    private var onCancelled: (() -> Unit)? = null

    private var systemRequestLauncher: ActivityResultLauncher<String?>? = null

    fun setTitle(title: String): RationaleDialogBuilder {
      this.dialogTitle = title
      return this
    }

    fun setMessage(message: String): RationaleDialogBuilder {
      this.dialogMessage = message
      return this
    }

    fun setAcceptedButtonText(text: String): RationaleDialogBuilder {
      this.acceptedButtonText = text
      return this
    }

    fun setOnAccepted(onAccepted: () -> Unit): RationaleDialogBuilder {
      this.onAccepted = onAccepted
      return this
    }

    fun setDismissedButtonText(text: String): RationaleDialogBuilder {
      this.dismissedButtonText = text
      return this
    }

    fun setOnDismissed(onDismissed: () -> Unit): RationaleDialogBuilder {
      this.onDismissed = onDismissed
      return this
    }

    fun setOnCancelled(onCancelled: () -> Unit): RationaleDialogBuilder {
      this.onCancelled = onCancelled
      return this
    }

    fun setActivityResultLauncher(arl: ActivityResultLauncher<String?>): RationaleDialogBuilder {
      this.systemRequestLauncher = arl
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
        .setPositiveButton(acceptedButtonText) { _, _ -> showSystemPermissionsRequest() }
        .setNegativeButton(dismissedButtonText) { _, _ -> }
        .create()

      dialog.show()
    }

    private fun showSystemPermissionsRequest() {
      if (systemRequestLauncher == null) {
        Log.e(TAG, "showSystemPermissionsRequest: activity result launcher is null. Did " +
            "you forget to call setActivityResultLauncher() in the RequestBuilder?")
      }
      systemRequestLauncher?.launch(permission)
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