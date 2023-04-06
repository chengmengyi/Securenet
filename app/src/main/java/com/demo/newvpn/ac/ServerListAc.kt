package com.demo.newvpn.ac

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.newvpn.R
import com.demo.newvpn.adapter.ServerAdapter
import com.demo.newvpn.admob.LoadAd
import com.demo.newvpn.admob.ShowFullAd
import com.demo.newvpn.admob.ShowNativeAd
import com.demo.newvpn.base.BaseAc
import com.demo.newvpn.bean.ServerBean
import com.demo.newvpn.config.Local
import com.demo.newvpn.server.ConnectServerUtil
import com.demo.newvpn.util.LimitManager
import kotlinx.android.synthetic.main.activity_server.*

class ServerListAc:BaseAc() {
    private val showNativeAd by lazy { ShowNativeAd(Local.SERVER_TOP,this) }
    private val showBackAd by lazy { ShowFullAd(Local.BACK,this) }

    override fun layout(): Int = R.layout.activity_server

    override fun initView() {
        immersionBar.statusBarView(top).init()
        LoadAd.load(Local.BACK)

        rv_server.apply {
            layoutManager=LinearLayoutManager(this@ServerListAc)
            adapter=ServerAdapter(this@ServerListAc){ item(it) }
        }

        iv_back.setOnClickListener { onBackPressed() }
    }

    private fun item(serverBean: ServerBean){
        val current = ConnectServerUtil.currentServer
        val connected =ConnectServerUtil.isConnected()
        if(connected&&current.ip!=serverBean.ip){
            AlertDialog.Builder(this).apply {
                setMessage("If you want to connect to another VPN, you need to disconnect the current connection first. Do you want to disconnect the current connection?")
                setPositiveButton("sure") { _, _ ->
                    chooseBackHome(serverBean,"dis_0322")
                }
                setNegativeButton("cancel",null)
                show()
            }
        }else{
            if (connected){
                chooseBackHome(serverBean,"")
            }else{
                chooseBackHome(serverBean,"con_0322")
            }
        }
    }


    private fun chooseBackHome(serverBean: ServerBean,result:String){
        ConnectServerUtil.currentServer=serverBean
        setResult(322, Intent().apply {
            putExtra("back_0322",result)
        })
        finish()
    }

    override fun onBackPressed() {
        showBackAd.showFull(
            back = true,
            showing = {},
            close = {
                finish()
            }
        )
    }

    override fun onResume() {
        super.onResume()
        showNativeAd.checkHasNativeAd(showMedia = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        showNativeAd.stopShow()
        LimitManager.setRefreshStatus(Local.SERVER_TOP,true)
    }
}