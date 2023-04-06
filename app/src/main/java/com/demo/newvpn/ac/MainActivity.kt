package com.demo.newvpn.ac


import android.animation.ValueAnimator
import android.content.Intent
import android.view.KeyEvent
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import com.blankj.utilcode.util.ActivityUtils
import com.demo.newvpn.R
import com.demo.newvpn.admob.LoadAd
import com.demo.newvpn.admob.ShowFullAd
import com.demo.newvpn.base.BaseAc
import com.demo.newvpn.config.Fire
import com.demo.newvpn.config.Local
import com.demo.newvpn.server.ConnectServerUtil
import com.demo.newvpn.util.LimitManager
import com.demo.newvpn.util.ReferrerManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseAc() {
    private var coldLoad=true
    private var animator: ValueAnimator?=null
    private val showOpenAd by lazy { ShowFullAd(Local.OPEN,this) }

    override fun layout(): Int = R.layout.activity_main

    override fun initView() {
        coldLoad=intent.getBooleanExtra("cold",true)
        ReferrerManager.readReferrer()
        LimitManager.resetRefresh()
        LimitManager.readNum()
        LoadAd.preLoadAd()
        startAnimator()
    }

    private fun startAnimator(){
        animator = ValueAnimator.ofInt(0, 100).apply {
            duration = 10000L
            interpolator = LinearInterpolator()
            addUpdateListener {
                val progress = it.animatedValue as Int
                progress_view.progress = progress
                val pro = (10 * (progress / 100.0F)).toInt()
                if(pro in 2..9){
                    showOpenAd.showFull(
                        showing = {
                            stopAnimator()
                            progress_view.progress = 100
                        },
                        close = {
                            checkPlan()
                        }
                    )
                }else if (pro>=10){
                    checkPlan()
                }
            }
            start()
        }
    }

    private fun checkPlan(){
        if(!ReferrerManager.isBuyUser()){
            toHomeAc()
            return
        }
        Fire.randomPlanB(coldLoad)
        toHomeAc(auto = Fire.planB&&ConnectServerUtil.isDisconnected())
    }

    private fun toHomeAc(auto:Boolean=false){
        startActivity(Intent(this,HomeAc::class.java).apply {
            putExtra("auto",auto)
        })
        finish()
    }

    private fun stopAnimator(){
        animator?.removeAllUpdateListeners()
        animator?.cancel()
        animator=null
    }

    override fun onResume() {
        super.onResume()
        if (animator?.isPaused==true){
            animator?.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        animator?.pause()

    }

    override fun onDestroy() {
        super.onDestroy()
        stopAnimator()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode== KeyEvent.KEYCODE_BACK){
            return true
        }
        return false
    }
}