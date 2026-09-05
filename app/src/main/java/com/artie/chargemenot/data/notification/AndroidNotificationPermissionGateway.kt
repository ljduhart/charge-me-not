package com.artie.chargemenot.data.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.artie.chargemenot.domain.repository.NotificationPermissionGateway

class AndroidNotificationPermissionGateway(
    private val context: Context
) : NotificationPermissionGateway {

    override fun isNotificationPermissionGranted(): Boolean {
        if (!requiresRuntimePermission()) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun requiresRuntimePermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
}
