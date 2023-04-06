package com.demo.newvpn.util

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.ActivityUtils
import com.demo.newvpn.ac.HomeAc
import com.demo.newvpn.ac.MainActivity
import com.demo.newvpn.interfaces.IAppFrontBackInterface
import com.google.android.gms.ads.AdActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AcRegister {
    var isFront=true
    private var hotReload=false
    private var job: Job?=null
    private var iAppFrontBackInterface:IAppFrontBackInterface?=null

    fun register(application: Application){
        application.registerActivityLifecycleCallbacks(callback)
    }

    fun setAppBackInterface(iAppFrontBackInterface:IAppFrontBackInterface?){
        this.iAppFrontBackInterface=iAppFrontBackInterface
    }

    private val callback=object : Application.ActivityLifecycleCallbacks{
        private var pages=0
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {
            pages++
            job?.cancel()
            job=null
            if (pages==1){
                isFront=true
                if (hotReload){
                    if (ActivityUtils.isActivityExistsInStack(HomeAc::class.java)){
                        activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                            putExtra("cold",false)
                        })
                    }
                }
                hotReload=false
            }
        }

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
            pages--
            if (pages<=0){
                isFront=false
                iAppFrontBackInterface?.appToBack()
                job= GlobalScope.launch {
                    delay(3000L)
                    hotReload=true
                    ActivityUtils.finishActivity(MainActivity::class.java)
                    ActivityUtils.finishActivity(AdActivity::class.java)
                }
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }
}