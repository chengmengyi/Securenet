package com.demo.newvpn.config

import android.webkit.WebView
import com.demo.newvpn.server.ServerInfoUtil
import com.demo.newvpn.util.LimitManager
import com.demo.newvpn.util.myApp
import com.demo.newvpn.util.str2Int
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.tencent.mmkv.MMKV
import org.json.JSONObject
import java.util.*

object Fire {
    var isLimitUser=false
    private var securenet_start="1"
    private var securenet_ratio="100"
    var planB=false

    fun readFire(){
        checkIsLimitUser()
        LimitManager.setNum(Local.localAdStr2)
//        val remoteConfig = Firebase.remoteConfig
//        remoteConfig.fetchAndActivate().addOnCompleteListener {
//            if (it.isSuccessful){
//                ServerInfoUtil.parseServerJson(false, string = remoteConfig.getString("securenet_serve"))
//                ServerInfoUtil.parseCityList(remoteConfig.getString("securenet_fast"))
//                saveAdJson(remoteConfig.getString("securenet_ad"))
//                parsePlanBJson(remoteConfig.getString("securenet_config"))
//            }
//        }
    }

    private fun parsePlanBJson(string: String){
        runCatching {
            val jsonObject = JSONObject(string)
            securenet_start=jsonObject.optString("securenet_start")
            securenet_ratio=jsonObject.optString("securenet_ratio")
        }
    }

    private fun saveAdJson(string: String){
        runCatching {
            LimitManager.setNum(string)
            MMKV.defaultMMKV().encode("securenet_ad",string)
        }
    }

    fun getAdJson():String{
        val value = MMKV.defaultMMKV().decodeString("securenet_ad") ?: ""
        if(value.isEmpty()){
            return Local.localAdStr2
        }
        return value
    }

    fun randomPlanB(coldLoad:Boolean){
        if((coldLoad&&securenet_start=="1")||securenet_start=="2"){
            val nextInt = Random().nextInt(100)
            planB = str2Int(securenet_ratio)>=nextInt
        }
    }

    private fun checkIsLimitUser(){
        OkGo.get<String>("https://ipapi.co/json")
            .headers("User-Agent", WebView(myApp).settings.userAgentString)
            .execute(object : StringCallback(){
                override fun onSuccess(response: Response<String>?) {
                    try {
                        isLimitUser = JSONObject(response?.body()?.toString()).optString("country_code").limitArea()
                    }catch (e:Exception){

                    }
                }
            })
    }

    private fun String.limitArea()=contains("IR")||contains("MO")||contains("HK")||contains("CN")
}