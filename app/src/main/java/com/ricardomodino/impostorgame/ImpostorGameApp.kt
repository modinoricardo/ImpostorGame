package com.ricardomodino.impostorgame

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.ricardomodino.impostorgame.data.local.AppDatabase
import com.ricardomodino.impostorgame.data.local.DatabaseSeeder
import com.ricardomodino.impostorgame.data.remote.SupabaseSync
import com.ricardomodino.impostorgame.data.repository.ContentRepository
import com.ricardomodino.impostorgame.managers.AdsManager
import com.ricardomodino.impostorgame.managers.ImmersiveModeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImpostorGameApp : Application() {

    override fun onCreate() {
        super.onCreate()

        AdsManager.init(this)

        // Sembrar la BD en background y sincronizar con Supabase
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(this@ImpostorGameApp)
            val repo = ContentRepository(db)
            if (!repo.isSeeded()) {
                DatabaseSeeder.seed(this@ImpostorGameApp, db)
            }
            SupabaseSync.sync(this@ImpostorGameApp, db)
        }

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
