package uk.ac.shef.oak.com6510.activities

/*
 * Copyright (c) 2019. This code has been developed by Fabio Ciravegna, The University of Sheffield.
 * Updated 2021 by Temitope Adeosun, using Kotlin with MVVM and LiveData implementation
 * All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.lang.Exception

class Temperature(context: Context) {
    private val BAROMETER_READING_FREQ_MICRO_SEC: Int = 30000
    private var samplingRateInMicroSec: Long = BAROMETER_READING_FREQ_MICRO_SEC.toLong()
    private var samplingRateInNanoSec: Long = samplingRateInMicroSec * 1000
    private var timePhoneWasLastRebooted: Long = 0
    private var lastReportTime: Long = 0

    //    private lateinit var accelerometer: Accelerometer
    private var sensorManager: SensorManager?
    private var temperatureSensor: Sensor
    private var temperatureEventListener: SensorEventListener? = null
    private var _isStarted = false
    val isStarted: Boolean
        get() {return _isStarted}

    var temperatureReading: MutableLiveData<Float> = MutableLiveData<Float>()


    init{
        // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
        timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime()

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        temperatureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)!!

        /**
         * this inits the listener and establishes the actions to take when a sensor is available
         * It is not registere to listen at this point, but makes sure the object is available to
         * listen when registered.
         */
        temperatureEventListener  = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                temperatureReading.value = event.values[0]
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    companion object {
        private val TAG = Temperature::class.java.simpleName

        /**
         * this is used to stop the temperature if we have not seen any movement in the last 20 seconds
         */
        private const val STOPPING_THRESHOLD = 20000.toLong()
    }

    /**
     * it starts the pressure monitoring and updates the _isStarted status flag
     */
    fun startTemperatureSensing() {
//        this.accelerometer = accelerometer
        sensorManager?.let {
            // if the sensor is null,then mSensorManager is null and we get a crash
            Log.d(TAG, "Starting listener")
            // delay is in microseconds (1millisecond=1000 microseconds)
            // it does not seem to work though
            //stopTemperature();
            // otherwise we stop immediately because
            it.registerListener(
                temperatureEventListener,
                temperatureSensor,
                samplingRateInMicroSec.toInt()
            )
            _isStarted = true
        }
    }

    /**
     * this stops the temperature and updates the _isStarted status flag
     */
    fun stopTemperatureSensing() {
        sensorManager?.let {
            Log.d(TAG, "Stopping listener")
            try {
                it.unregisterListener(temperatureEventListener)
                _isStarted = false
            } catch (e: Exception) {
                // probably already unregistered
                println("failed to unregister sensor, probably not running already")
            }
        }
    }


}