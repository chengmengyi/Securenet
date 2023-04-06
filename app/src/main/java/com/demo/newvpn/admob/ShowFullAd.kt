package com.demo.newvpn.admob

import com.demo.newvpn.base.BaseAc
import com.demo.newvpn.config.Local
import com.demo.newvpn.util.LimitManager
import com.demo.newvpn.util.qwer
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShowFullAd(
    private val type:String,
    private val baseAc: BaseAc
) {
    private lateinit var close:()->Unit

    fun showFull(back:Boolean=false,showing:()->Unit,close:()->Unit){
        this.close=close
        val ad = LoadAd.getAdByType(type)
        if (null!=ad){
            if (LoadAd.fullShowing||!baseAc.resume){
                close.invoke()
                return
            }
            qwer("showing $type ad")
            showing.invoke()
            when(ad){
                is InterstitialAd ->{
                    ad.fullScreenContentCallback= fullCallback
                    ad.show(baseAc)
                }
                is AppOpenAd ->{
                    ad.fullScreenContentCallback= fullCallback
                    ad.show(baseAc)
                }
            }
        }else{
            if (back){
                LoadAd.load(type)
                close.invoke()
            }
        }
    }

    private val fullCallback= object : FullScreenContentCallback(){
        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()
            LoadAd.fullShowing =false
            onCloseAd()
        }

        override fun onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent()
            LoadAd.fullShowing  =true
            LimitManager.updateShow()
            LoadAd.removeAd(type)
        }

        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            super.onAdFailedToShowFullScreenContent(p0)
            LoadAd.fullShowing  =false
            LoadAd.removeAd(type)
            onCloseAd()
        }


        override fun onAdClicked() {
            super.onAdClicked()
            LimitManager.updateClick()
        }
    }

    private fun onCloseAd(){
        if (type!= Local.OPEN||type!= Local.BACK){
            LoadAd.load(type)
        }
        GlobalScope.launch(Dispatchers.Main) {
            delay(200L)
            if (baseAc.resume){
                close.invoke()
            }
        }
    }
}