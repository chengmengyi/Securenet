package com.demo.newvpn.ac

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.VpnService
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.blankj.utilcode.util.ActivityUtils
import com.demo.newvpn.R
import com.demo.newvpn.admob.LoadAd
import com.demo.newvpn.admob.ShowFullAd
import com.demo.newvpn.admob.ShowNativeAd
import com.demo.newvpn.base.BaseAc
import com.demo.newvpn.config.Fire
import com.demo.newvpn.config.Local
import com.demo.newvpn.interfaces.IAppFrontBackInterface
import com.demo.newvpn.interfaces.IConnectInterface
import com.demo.newvpn.interfaces.ITimeInterface
import com.demo.newvpn.server.ConnectServerUtil
import com.demo.newvpn.server.ConnectServerUtil.connectServerSuccess
import com.demo.newvpn.server.TimeUtil
import com.demo.newvpn.util.*
import com.github.shadowsocks.utils.StartService
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.drawer_home.*
import kotlinx.coroutines.*
import java.lang.Exception

class HomeAc:BaseAc(), IConnectInterface, ITimeInterface, IAppFrontBackInterface {
    private var jobTime=-1
    private var canClick=true
    private var permission=false
    private var autoConnect=false
    private var connect=false
    private var connectServerJob:Job?=null
    private val showHomeAd by lazy { ShowNativeAd(Local.HOME_BOTTOM,this) }
    private val showConnectAd by lazy { ShowFullAd(Local.CONNECT,this) }

    private val registerResult=registerForActivityResult(StartService()) {
        if (!it && permission) {
            permission = false
            startConnectServer()
        } else {
            canClick=true
            toast("Connected fail")
        }
    }

    override fun layout(): Int = R.layout.activity_home

    override fun initView() {
        immersionBar.statusBarView(top).init()
        TimeUtil.setInterface(this)
        ConnectServerUtil.init(this,this)
        AcRegister.setAppBackInterface(this)
        updateServerInfo()
        setClick()
        checkAutoConnect(intent)
        if(ConnectServerUtil.isConnected()){
            hideGuideView()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { checkAutoConnect(it) }
    }

    private fun checkAutoConnect(intent: Intent){
        if(intent.getBooleanExtra("auto",false)){
            autoConnect=true
            tv_connect.performClick()
        }
    }

    private fun setClick(){
        tv_connect.setOnClickListener { doConnectLogic() }
        llc_choose_server.setOnClickListener {
            if(canClick&&!drawer_layout.isOpen){
                startActivityForResult(Intent(this,ServerListAc::class.java),322)
            }
            if (!connect&&ConnectServerUtil.isConnected()){
                appToBack()
            }
        }

        guide_view.setOnClickListener {  }
        guide_lottie_view.setOnClickListener { tv_connect.performClick() }

        iv_set.setOnClickListener {
            if(canClick&&!drawer_layout.isOpen){
                drawer_layout.openDrawer(Gravity.LEFT)
            }
            if (!connect&&ConnectServerUtil.isConnected()){
                appToBack()
            }
        }
        tv_contact.setOnClickListener {
            if(canClick&&drawer_layout.isOpen){
                try {
                    val uri = Uri.parse("mailto:${Local.EMAIL}")
                    val intent = Intent(Intent.ACTION_SENDTO, uri)
                    startActivity(intent)
                }catch (e: Exception){
                    toast("Contact us by email：${Local.EMAIL}")
                }
            }
        }
        tv_update.setOnClickListener {
            if(canClick&&drawer_layout.isOpen){
                val packName = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).packageName
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$packName")
                }
                startActivity(intent)
            }
        }
        tv_policy.setOnClickListener {
            if(canClick&&drawer_layout.isOpen){
                startActivity(Intent(this,WebAc::class.java))
            }
        }
        tv_share.setOnClickListener {
            if(canClick&&drawer_layout.isOpen){
                val pm = packageManager
                val packageName=pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).packageName
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=${packageName}"
                )
                startActivity(Intent.createChooser(intent, "share"))
            }
        }
    }

    private fun doConnectLogic(){
        if(Fire.isLimitUser){
            AlertDialog.Builder(this).apply {
                setCancelable(false)
                setMessage("Due to the policy reason , this service is not available in your country")
                setPositiveButton("confirm") { _, _ ->
                    finish()
                }
                show()
            }
            return
        }
        LoadAd.load(Local.CONNECT)
        LoadAd.load(Local.RESULT)
        if(!canClick||drawer_layout.isOpen){
            return
        }
        canClick=false
        if(ConnectServerUtil.isConnected()){
            updateStoppingUI()
            startConnectServerJob(false)
        }else{
            updateServerInfo()
            if (getNetStatus()==1){
                AlertDialog.Builder(this).apply {
                    setMessage("Network request timed out. Please make sure your network is connected")
                    setPositiveButton("OK") { _, _ ->

                    }
                    show()
                }
                canClick=true
                return
            }
            if (VpnService.prepare(this) != null) {
                permission = true
                registerResult.launch(null)
                return
            }

            startConnectServer()
        }
    }

    private fun startConnectServer(){
        TimeUtil.resetTime()
        hideGuideView()
        updateConnectingUI()
        startConnectServerJob(true)
    }

    private fun startConnectServerJob(connect:Boolean){
        jobTime=0
        this.connect=connect
        connectServerJob= GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                if (!isActive) {
                    break
                }
                delay(1000)
                jobTime++
                if (jobTime==3){
                    if (connect){
                        ConnectServerUtil.connect()
                    }else{
                        ConnectServerUtil.disconnect()
                    }
                }

                withContext(Dispatchers.Main){
                    if (jobTime in 3..9){
                        if (connectServerSuccess(connect)){
                            if(null!= LoadAd.getAdByType(Local.CONNECT)){
                                cancel()
                                showConnectAd.showFull(
                                    showing = {
                                        connectJobFinish(connect,toResult = false)
                                    },
                                    close = {
                                        connectJobFinish(connect)
                                    }
                                )
                            }else{
                                if(LimitManager.hasLimit()){
                                    cancel()
                                    connectJobFinish(connect)
                                }
                            }
                        }
                    }else if (jobTime >= 10) {
                        cancel()
                        connectJobFinish(connect)
                    }
                }
            }
        }
    }

    private fun connectJobFinish(connect: Boolean,toResult:Boolean=true){
        runOnUiThread {
            if (connectServerSuccess(connect)){
                if (connect){

                }else{
                    updateStoppedUI()
                    updateServerInfo()
                }
                if(toResult&&connect){
                    updateConnectedUI()
                    TimeUtil.start()
                }
                if (toResult&&AcRegister.isFront&&ActivityUtils.getTopActivity().javaClass.name==HomeAc::class.java.name){
                    startActivity(Intent(this,ResultAc::class.java).apply {
                        putExtra("connect",connect)
                    })
                }
            }else{
                updateStoppedUI()
                toast(if (connect) "Connect Fail" else "Disconnect Fail")
            }
            canClick=true
        }
    }

    override fun connectSuccess() {
        updateConnectedUI()
    }

    override fun disConnectSuccess() {
        if (canClick){
            updateStoppedUI()
        }
    }

    private fun updateStoppedUI(){
        tv_connect.text="Connect"
        tv_time.isSelected=false
        tv_time.text="00:00:00"
        iv_status.setImageResource(R.drawable.connect)
    }

    private fun updateConnectingUI(){
        tv_connect.text="Connecting…"
        tv_time.isSelected=false
        iv_status.setImageResource(R.drawable.connecting)
    }

    private fun updateStoppingUI(){
        tv_connect.text="Disconnecting…"
        tv_time.isSelected=false
        iv_status.setImageResource(R.drawable.connecting)
    }

    private fun updateConnectedUI(){
        tv_connect.text="Disconnect"
        tv_time.isSelected=true
        iv_status.setImageResource(R.drawable.connected)
    }

    private fun updateServerInfo(){
        val currentServer = ConnectServerUtil.currentServer
        tv_name.text=currentServer.country
        iv_logo.setImageResource(getLogo(currentServer.country))
    }

    override fun connectTimeCallback(time: String) {
        tv_time.text=time
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==322){
            when(data?.getStringExtra("back_0322")){
                "dis_0322"->{
                    doConnectLogic()
                }
                "con_0322"->{
                    updateServerInfo()
                    doConnectLogic()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showHomeAd.checkHasNativeAd()
    }

    override fun onDestroy() {
        super.onDestroy()
        showHomeAd.stopShow()
        stopConnectJob()
        ConnectServerUtil.onDestroy()
        TimeUtil.setInterface(this)
        LimitManager.setRefreshStatus(Local.HOME_BOTTOM,true)
    }

    private fun stopConnectJob(){
        connectServerJob?.cancel()
        connectServerJob=null
    }

    override fun onBackPressed() {
        if(canClick){
            if(guide_lottie_view.visibility==View.VISIBLE){
                hideGuideView()
            }else{
                finish()
            }
        }
        if (!connect&&ConnectServerUtil.isConnected()){
            appToBack()
        }
    }

    private fun hideGuideView(){
        guide_view.show(false)
        guide_lottie_view.show(false)
        tv_connect.show(true)
    }

    override fun appToBack() {
        if(jobTime in 0..2){
            jobTime=-1
            canClick=true
            stopConnectJob()
            if(connect){
                updateStoppedUI()
            }else{
                ConnectServerUtil.currentServer=ConnectServerUtil.lastServer
                updateConnectedUI()
            }
        }
    }
}