package com.demo.newvpn.util

import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.tencent.mmkv.MMKV

object ReferrerManager {
    fun readReferrer(){
        if(readLocalReferrer().isEmpty()){
            val referrerClient = InstallReferrerClient.newBuilder(myApp).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    try {
                        when (responseCode) {
                            InstallReferrerClient.InstallReferrerResponse.OK -> {
                                val installReferrer = referrerClient.installReferrer.installReferrer
                                MMKV.defaultMMKV().encode("securenet_config_referrer",installReferrer)
                            }
                        }
                    } catch (e: Exception) {

                    }
                    try {
                        referrerClient.endConnection()
                    }catch (e:Exception){

                    }
                }
                override fun onInstallReferrerServiceDisconnected() {
                }
            })
        }
    }

    fun isBuyUser():Boolean{
        val localReferrer = readLocalReferrer()
        return localReferrer.contains("fb4a")||
                localReferrer.contains("gclid")||
                localReferrer.contains("not%20set")||
                localReferrer.contains("youtubeads")||
                localReferrer.contains("%7B%22")
    }


    private fun isFB():Boolean{
        val localReferrer = readLocalReferrer()
        return localReferrer.contains("fb4a")|| localReferrer.contains("facebook")
    }

    private fun readLocalReferrer()= MMKV.defaultMMKV().decodeString("securenet_config_referrer")?:""
}