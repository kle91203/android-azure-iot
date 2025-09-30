package com.summerlandsoftware.iot.kindle

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.microsoft.azure.sdk.iot.device.DeviceClient
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol
import java.util.concurrent.Executors

class MainActivity : Activity(), SensorEventListener {
    private val executor = Executors.newSingleThreadExecutor()
    private var deviceClient: DeviceClient? = null
    private val sensors = mutableMapOf<Int, Sensor?>()
    private lateinit var sensorManager: SensorManager
    private final val LOG_IDENTIFIER = "MainActivity"
    private val accelLastValues = floatArrayOf(0f, 0f, 0f);

    private var accel: Sensor? = null
    private var light: Sensor? = null

    private fun log(msg: String)
    {
        Log.i(LOG_IDENTIFIER, msg)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {

            //ignore for now (y is working correctly. need to test the other two -- one of them is not working corrrectly)
//            //ctivity: Deltas: x:0.04605, y:10.053, z:10.053, Values: x: 0.04605, y: 0.04605, z: 0.04605, Prev Values: x:0.0, y:0.0, z:0.0
//            val xPrev = accelLastValues[0]
//            val yPrev = accelLastValues[1]
//            val zPrev = accelLastValues[2]
//
//            val xCurr = event.values[0]
//            val yCurr = event.values[1]
//            val zCurr = event.values[2]
//
//            val xDelta = abs(xPrev - xCurr)
//            val yDelta = abs(yPrev - yCurr)
//            val zDelta = abs(zPrev - zCurr)
//
////            if (xDelta > 0.1 || yDelta > 0.1 || zDelta > 0.1) {
//            if (yDelta > 0.1) {
//
////                log("Accelerometer\n" +
////                        "Values: x:$xCurr, y:$yCurr, z:$zCurr, " +
////                        "Prev Values: x:$xPrev, y:$yPrev, z:$zPrev, " +
////                        "Deltas: x:$xDelta, y:$yDelta, z:$zDelta ")
//
//                log("Accelerometer\n" +
//                        "   Values: y:$yCurr\n" +
//                        "   Prev  : y:$yPrev\n" +
//                        "   Deltas: y:$yDelta\n")
//                accelLastValues[0] = xCurr
//                accelLastValues[1] = yCurr
//                accelLastValues[2] = zCurr
//                log("   --new value: ${accelLastValues[1]}")
//            }
        }
        else if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            log("Type: Light. Value: ${event.values[0]}")
        }
        else {
            log("Unrecognized type: ${event?.sensor?.type}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("SENSORS", "Accuracy changed: ${sensor?.name} -> $accuracy")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensors.put(Sensor.TYPE_LIGHT, light)
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensors.put(Sensor.TYPE_ACCELEROMETER, accel)

        if (accel == null) log("No accelerometer on this device")
        if (light == null) log("No light sensor on this device")

        val root = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val tv = TextView(this).apply {
            text = "Azure IoT: Connecting..."
            textSize = 24f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        }

        root.addView(tv)
        setContentView(root)

        // Start Azure IoT connection
        connectToIoTHub { status ->
            runOnUiThread {
                tv.text = "Azure IoT: $status"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register in onResume so the Activity is active
        accel?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME // try SENSOR_DELAY_NORMAL if you want
            )
        }
        light?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        log( "Registered listeners (accel=${accel != null}, light=${light != null})")
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        log("Unregistered listeners")
    }

    private fun connectToIoTHub(onStatus: (String) -> Unit) {
        executor.execute {
            try {
                val connStr = BuildConfig.IOTHUB_CONNECTION_STRING
                deviceClient = DeviceClient(connStr, IotHubClientProtocol.MQTT)
                deviceClient?.open(true)
                Log.i("MainActivity", "Connected to IoT Hub")
                onStatus("Connected ✅")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to connect to IoT Hub", e)
                onStatus("Error ❌")
            }
        }
    }
}
