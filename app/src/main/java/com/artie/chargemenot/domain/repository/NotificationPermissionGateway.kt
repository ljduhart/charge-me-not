package com.artie.chargemenot.domain.repository

interface NotificationPermissionGateway {
    fun isNotificationPermissionGranted(): Boolean
    fun requiresRuntimePermission(): Boolean
}
