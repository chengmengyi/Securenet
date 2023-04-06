package com.demo.newvpn.util

import android.app.Application
import com.demo.newvpn.ac.HomeAc
import com.demo.newvpn.config.Fire
import com.demo.newvpn.server.ServerInfoUtil
import com.github.shadowsocks.Core
import com.tencent.mmkv.MMKV

lateinit var myApp: MyApp
class MyApp:Application() {
    override fun onCreate() {
        super.onCreate()
        myApp=this
        Core.init(this, HomeAc::class)
        if (!packageName.equals(processName(this))){
            return
        }
        MMKV.initialize(this)
        ServerInfoUtil.parseServerJson(true)
        Fire.readFire()
        AcRegister.register(this)
    }
}