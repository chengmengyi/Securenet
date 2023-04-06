package com.demo.newvpn.ac

import com.demo.newvpn.R
import com.demo.newvpn.admob.ShowNativeAd
import com.demo.newvpn.base.BaseAc
import com.demo.newvpn.config.Local
import com.demo.newvpn.interfaces.ITimeInterface
import com.demo.newvpn.server.ConnectServerUtil
import com.demo.newvpn.server.TimeUtil
import com.demo.newvpn.util.LimitManager
import kotlinx.android.synthetic.main.activity_result.*

class ResultAc:BaseAc(), ITimeInterface {
    private var connect=false
    private val showResultAd by lazy { ShowNativeAd(Local.RESULT,this) }

    override fun layout(): Int = R.layout.activity_result

    override fun initView() {
        immersionBar.statusBarView(top).init()
        iv_back.setOnClickListener { finish() }
        connect=intent.getBooleanExtra("connect",false)
        if(connect){
            tv_title.text="Connected"
            iv_status.setImageResource(R.drawable.connected)
            tv_time.isSelected=true
            TimeUtil.setInterface(this)
        }else{
            tv_title.text="Disconnected"
            iv_status.setImageResource(R.drawable.connect)
            tv_time.isSelected=false
            tv_time.text=TimeUtil.getTotalTime()
        }
        tv_name.text=ConnectServerUtil.lastServer.country
    }

    override fun connectTimeCallback(time: String) {
        tv_time.text=time
    }

    override fun onResume() {
        super.onResume()
        showResultAd.checkHasNativeAd()
    }

    override fun onDestroy() {
        super.onDestroy()
        showResultAd.stopShow()
        LimitManager.setRefreshStatus(Local.RESULT,true)
        if(connect){
            TimeUtil.setInterface(this)
        }
    }
}