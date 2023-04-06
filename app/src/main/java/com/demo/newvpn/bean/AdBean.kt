package com.demo.newvpn.bean

class AdBean(
    val securenet_ab:String,
    val securenet_ad:String,
    val securenet_ac:String,
    val securenet_ae:Int,
) {
    override fun toString(): String {
        return "AdBean(securenet_ab='$securenet_ab', securenet_ad='$securenet_ad', securenet_ac='$securenet_ac', securenet_ae=$securenet_ae)"
    }
}