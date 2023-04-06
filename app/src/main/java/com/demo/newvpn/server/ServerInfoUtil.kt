package com.demo.newvpn.server

import com.demo.newvpn.bean.ServerBean
import com.demo.newvpn.config.Local
import org.json.JSONArray

object ServerInfoUtil {

    private val cityList= arrayListOf<String>()
    private val localServerList= arrayListOf<ServerBean>()
    private val fireServerList= arrayListOf<ServerBean>()

    fun getServerList()= fireServerList.ifEmpty { localServerList }

    fun getFastServer():ServerBean{
        val serverList = getServerList()
        if (!cityList.isNullOrEmpty()){
            val filter = serverList.filter { cityList.contains(it.city) }
            if (!filter.isNullOrEmpty()){
                return filter.random()
            }
        }
        return serverList.random()
    }

    fun parseServerJson(local:Boolean,string: String=""){
        if(local){
            parse(Local.localServer, localServerList)
        }else{
            parse(string,fireServerList)
        }
    }

    private fun parse(string:String,list:ArrayList<ServerBean>){
        runCatching {
            list.clear()
            val jsonArray = JSONArray(string)
            for (index in 0 until jsonArray.length()){
                val jsonObject = jsonArray.getJSONObject(index)
                list.add(
                    ServerBean(
                        pwd = jsonObject.optString("pwd"),
                        account = jsonObject.optString("encryption"),
                        port = jsonObject.optInt("port"),
                        country =jsonObject.optString("country"),
                        city =jsonObject.optString("city"),
                        ip=jsonObject.optString("ip")
                    )
                )
            }
            list.forEach { it.writeServerId() }
        }
    }


    fun parseCityList(string: String){
        runCatching {
            val jsonArray = JSONArray(string)
            for (index in 0 until jsonArray.length()){
                cityList.add(jsonArray.optString(index))
            }
        }
    }
}