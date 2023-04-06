package com.demo.newvpn.util

import com.tencent.mmkv.MMKV
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object LimitManager {

    private var securenet_show=50
    private var securenet_click=15

    private var click=0
    private var show=0

    private val refresh= hashMapOf<String,Boolean>()

    fun resetRefresh(){
        refresh.clear()
    }

    fun canRefresh(type:String)=refresh[type]?:true

    fun setRefreshStatus(type:String,boolean: Boolean){
        refresh[type]=boolean
    }

    fun setNum(string: String){
        try{
            val jsonObject = JSONObject(string)
            securenet_show=jsonObject.optInt("securenet_show")
            securenet_click=jsonObject.optInt("securenet_click")
        }catch (e:Exception){

        }
    }

    fun readNum(){
        click= MMKV.defaultMMKV().decodeInt(key("securenet_click"),0)
        show= MMKV.defaultMMKV().decodeInt(key("securenet_show"),0)
    }

    fun updateClick(){
        click++
        MMKV.defaultMMKV().encode(key("securenet_click"), click)
    }

    fun updateShow(){
        show++
        MMKV.defaultMMKV().encode(key("securenet_show"), show)
    }

    fun hasLimit()= click>= securenet_click|| show>= securenet_show

    private fun key(string:String)="${string}...${SimpleDateFormat("yyyy-MM-dd").format(Date(System.currentTimeMillis()))}"
}