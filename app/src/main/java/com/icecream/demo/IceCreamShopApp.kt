package com.icecream.demo

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Application class for Ice Cream Shop demo app.
 */
class IceCreamShopApp : Application(), LifecycleObserver {

    companion object {
        lateinit var instance: IceCreamShopApp
            private set

        fun applicationContext(): Context {
            return instance.applicationContext
        }

        var inForeground: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        
        // Register lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        instance = this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        inForeground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        inForeground = false
    }
}

