package com.triptracker.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class SensorViewModel(application: Application): AndroidViewModel(application){
    private var barometer: Barometer = Barometer(application)


    fun startSensing(){
        barometer.startBarometerSensing()
    }

    fun  stopSensing(){
        barometer.stopBarometerSensing()
    }

    /**
     * Func that exposes the pressure as LiveData to the View object
     * @return
     */
    fun retrievePressureData(): LiveData<Float> {
        return barometer.pressureReading
    }

}