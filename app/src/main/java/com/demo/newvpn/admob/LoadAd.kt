package com.demo.newvpn.admob

import com.demo.newvpn.bean.AdBean
import com.demo.newvpn.bean.AdResultBean
import com.demo.newvpn.config.Fire
import com.demo.newvpn.config.Local
import com.demo.newvpn.util.LimitManager
import com.demo.newvpn.util.myApp
import com.demo.newvpn.util.qwer
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions
import org.json.JSONObject

object LoadAd {
    var fullShowing=false
    private val loading= arrayListOf<String>()
    private val adMap= hashMapOf<String,AdResultBean>()

    fun load(type:String,tryNum:Int=0){
        if(LimitManager.hasLimit()){
            qwer("limit")
            return
        }
        if(loading.contains(type)){
            qwer("$type  loading")
            return
        }

        if(adMap.containsKey(type)){
            val resultAdBean = adMap[type]
            if(null!=resultAdBean?.ad){
                if(resultAdBean.expired()){
                    removeAd(type)
                }else{
                    qwer("$type cache")
                    return
                }
            }
        }
        val parseAdList = getAdListByType(type)
        if(parseAdList.isEmpty()){
            qwer("$type ad empty")
            return
        }
        loading.add(type)
        loopLoad(type,parseAdList.iterator(),tryNum)
    }

    private fun loopLoad(type: String , iterator: Iterator<AdBean>, tryNum:Int){
        startLoadAd(type,iterator.next()){
            if(null!=it){
                loading.remove(type)
                adMap[type]=it
            }else{
                if(iterator.hasNext()){
                    loopLoad(type,iterator,tryNum)
                }else{
                    loading.remove(type)
                    if(tryNum>0&&type==Local.OPEN){
                        load(type, tryNum = 0)
                    }
                }
            }
        }
    }

    private fun startLoadAd(type: String, adBean: AdBean, result: (bean: AdResultBean?) -> Unit){
        qwer("load ad ${adBean.toString()}")
        when(adBean.securenet_ac){
            "o"->{
                AppOpenAd.load(
                    myApp,
                    adBean.securenet_ad,
                    AdRequest.Builder().build(),
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    object : AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdLoaded(p0: AppOpenAd) {
                            qwer("load $type ad success")
                            result.invoke(AdResultBean(time = System.currentTimeMillis(), ad = p0))
                        }

                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            qwer("load $type ad fail---${p0.message}")
                            result.invoke(null)
                        }
                    }
                )
            }
            "inr"->{
                InterstitialAd.load(
                    myApp,
                    adBean.securenet_ad,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            qwer("load $type ad fail---${p0.message}")
                            result.invoke(null)
                        }

                        override fun onAdLoaded(p0: InterstitialAd) {
                            qwer("load $type ad success")
                            result.invoke(AdResultBean(time = System.currentTimeMillis(), ad = p0))
                        }
                    }
                )
            }
            "n"->{
                AdLoader.Builder(
                    myApp,
                    adBean.securenet_ad,
                ).forNativeAd {p0->
                    qwer("load $type ad success")
                    result.invoke(AdResultBean(time = System.currentTimeMillis(), ad = p0))
                }
                    .withAdListener(object : AdListener(){
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            qwer("load $type ad fail---${p0.message}")
                            result.invoke(null)
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            LimitManager.updateClick()
                        }
                    })
                    .withNativeAdOptions(
                        NativeAdOptions.Builder()
                            .setAdChoicesPlacement(
                                if(type==Local.RESULT) NativeAdOptions.ADCHOICES_BOTTOM_RIGHT
                                else  NativeAdOptions.ADCHOICES_TOP_LEFT
                            )
                            .build()
                    )
                    .build()
                    .loadAd(AdRequest.Builder().build())
            }
        }
    }

    private fun getAdListByType(type: String):List<AdBean>{
        val list= arrayListOf<AdBean>()
        runCatching {
            val jsonArray = JSONObject(Fire.getAdJson()).getJSONArray(type)
            for (index in 0 until jsonArray.length()){
                val jsonObject = jsonArray.getJSONObject(index)
                list.add(
                    AdBean(
                        jsonObject.optString("securenet_ab"),
                        jsonObject.optString("securenet_ad"),
                        jsonObject.optString("securenet_ac"),
                        jsonObject.optInt("securenet_ae"),
                    )
                )
            }

        }
        return list.filter { it.securenet_ab == "admob" }.sortedByDescending { it.securenet_ae }
    }

    fun removeAd(type: String){
        adMap.remove(type)
    }

    fun getAdByType(type: String)= adMap[type]?.ad

    fun preLoadAd(){
        load(Local.OPEN, tryNum = 1)
        load(Local.CONNECT)
        load(Local.RESULT)
        load(Local.HOME_BOTTOM)
        load(Local.SERVER_TOP)
    }

    fun removeAllAd(){
        adMap.clear()
        loading.clear()
        preLoadAd()
        load(Local.BACK)
    }
}