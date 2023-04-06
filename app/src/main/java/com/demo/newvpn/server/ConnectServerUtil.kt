package com.demo.newvpn.server

import com.demo.newvpn.admob.LoadAd
import com.demo.newvpn.base.BaseAc
import com.demo.newvpn.bean.ServerBean
import com.demo.newvpn.interfaces.IConnectInterface
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.preference.DataStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object ConnectServerUtil: ShadowsocksConnection.Callback {
    private var baseAc: BaseAc?=null
    var state = BaseService.State.Stopped
    var currentServer= ServerBean()
    var lastServer= ServerBean()
    var fastServer= ServerBean()
    private val sc= ShadowsocksConnection(true)
    private var iConnectInterface: IConnectInterface?=null

    fun init(baseAc:BaseAc,iConnectInterface: IConnectInterface){
        this.baseAc=baseAc
        this.iConnectInterface=iConnectInterface
        sc.connect(baseAc,this)
    }

    fun connect(){
        state= BaseService.State.Connecting
        GlobalScope.launch {
            if (currentServer.isSuperFast()){
                fastServer=ServerInfoUtil.getFastServer()
                DataStore.profileId = fastServer.getServerId()
            }else{
                DataStore.profileId = currentServer.getServerId()
            }
            Core.startService()
        }
    }

    fun disconnect(){
        state= BaseService.State.Stopping
        GlobalScope.launch {
            Core.stopService()
        }
    }

    fun isConnected()= state== BaseService.State.Connected

    fun isDisconnected()= state== BaseService.State.Stopped

    fun connectServerSuccess(connect: Boolean)=if (connect) isConnected() else isDisconnected()

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        this.state=state
        if (isConnected()){
            lastServer= currentServer
            LoadAd.removeAllAd()
//            iConnectInterface?.connectSuccess()
        }
        if (isDisconnected()){
            TimeUtil.end()
            iConnectInterface?.disConnectSuccess()
        }
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        val state = BaseService.State.values()[service.state]
        this.state=state
        if (isConnected()){
            lastServer= currentServer
            TimeUtil.start()
            iConnectInterface?.connectSuccess()
        }
    }

    override fun onBinderDied() {
        baseAc?.let {
            sc.disconnect(it)
        }
    }

    fun onDestroy(){
        onBinderDied()
        baseAc=null
        iConnectInterface=null
    }
}