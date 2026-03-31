package com.ricardomodino.impostorgame

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.ricardomodino.impostorgame.managers.ImmersiveModeManager

class ImpostorGameApp : Application() {

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.window.decorView.post {
                    ImmersiveModeManager.apply(activity)
                }
            }

            override fun onActivityResumed(activity: Activity) {
                activity.window.decorView.post {
                    ImmersiveModeManager.apply(activity)
                }
            }

            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}
