package uk.ac.shef.oak.com6510.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class SensorViewModel(application: Application): AndroidViewModel(application){
    private var barometer: Barometer = Barometer(application)
    private var temperature:Temperature = Temperature(application)


    /**
     * start barometer sensor and temperature sensor
     */
    fun startSensing(){
        barometer.startBarometerSensing()
        temperature.startTemperatureSensing()
    }

    /**
     * stop barometer sensor and temperature sensor
     */
    fun  stopSensing(){
        barometer.stopBarometerSensing()
        temperature.stopTemperatureSensing()
    }

    /**
     * Func that exposes the pressure as LiveData to the View object
     * @return
     */
    fun retrievePressureData(): LiveData<Float> {
        return barometer.pressureReading
    }

    /**
     * Func that exposes the temperature as LiveData to the View object
     * @return
     */
    fun retrieveTemperatureData():LiveData<Float>{
        return temperature.temperatureReading
    }

}