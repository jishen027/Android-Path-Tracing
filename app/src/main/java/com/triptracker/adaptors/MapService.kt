package com.triptracker.adaptors

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class MapService :Service(){
    private val TAG = "MapServices"

    override fun onCreate() {
        super.onCreate()

        Log.e(TAG,"onCreate")
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.e(TAG, "")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.e(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
    }
}