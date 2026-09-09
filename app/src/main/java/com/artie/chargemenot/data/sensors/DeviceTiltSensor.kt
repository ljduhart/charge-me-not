package com.artie.chargemenot.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlin.math.sqrt

interface DeviceTiltSensor {
    fun tiltOffsets(): Flow<Pair<Float, Float>>
}

class AndroidDeviceTiltSensor(
    context: Context
) : DeviceTiltSensor {

    private val sensorManager =
        context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gravitySensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    override fun tiltOffsets(): Flow<Pair<Float, Float>> = callbackFlow {
        val sensor = gravitySensor
        if (sensor == null) {
            trySend(0f to 0f)
            awaitClose { }
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                val xTilt = normalizeGravityComponent(event.values[0])
                val yTilt = normalizeGravityComponent(event.values[1])
                trySend(xTilt to yTilt)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_GAME
        )

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.conflate()

    private fun normalizeGravityComponent(component: Float): Float {
        return (component / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)
    }
}

class StationaryDeviceTiltSensor : DeviceTiltSensor {
    override fun tiltOffsets(): Flow<Pair<Float, Float>> = callbackFlow {
        trySend(0f to 0f)
        awaitClose { }
    }
}
