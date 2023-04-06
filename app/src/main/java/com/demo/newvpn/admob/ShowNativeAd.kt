package com.demo.newvpn.admob

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.blankj.utilcode.util.SizeUtils
import com.demo.newvpn.R
import com.demo.newvpn.base.BaseAc
import com.demo.newvpn.util.LimitManager
import com.demo.newvpn.util.qwer
import com.demo.newvpn.util.show
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.*

class ShowNativeAd(
    private val type:String,
    private val baseAc: BaseAc
) {
    private var lastNativeAd: NativeAd?=null
    private var showJob: Job?=null


    fun checkHasNativeAd(showMedia:Boolean=true){
        if(!LimitManager.canRefresh(type)){
            return
        }
        LoadAd.load(type)
        stopShow()
        showJob= GlobalScope.launch(Dispatchers.Main)  {
            delay(300L)
            if (!baseAc.resume){
                return@launch
            }
            while (true){
                if(!isActive){
                    break
                }
                val ad = LoadAd.getAdByType(type)
                if(baseAc.resume&&null!=ad&&ad is NativeAd){
                    cancel()
                    lastNativeAd?.destroy()
                    lastNativeAd=ad
                    startShowAd(ad,showMedia)
                }
                delay(1000L)
            }
        }
    }

    private fun startShowAd(ad: NativeAd, showMedia: Boolean) {
        qwer("show $type")
        val viewNative = baseAc.findViewById<NativeAdView>(R.id.native_view)
        viewNative.iconView=baseAc.findViewById(R.id.iv_logo_native)
        (viewNative.iconView as ImageFilterView).setImageDrawable(ad.icon?.drawable)

        viewNative.callToActionView=baseAc.findViewById(R.id.tv_install_native)
        (viewNative.callToActionView as AppCompatTextView).text=ad.callToAction

        if(showMedia){
            viewNative.mediaView=baseAc.findViewById(R.id.media_view_native)
            ad.mediaContent?.let {
                viewNative.mediaView?.apply {
                    setMediaContent(it)
                    setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(view: View?, outline: Outline?) {
                            if (view == null || outline == null) return
                            outline.setRoundRect(
                                0,
                                0,
                                view.width,
                                view.height,
                                SizeUtils.dp2px(8F).toFloat()
                            )
                            view.clipToOutline = true
                        }
                    }
                }
            }

        }

        viewNative.bodyView=baseAc.findViewById(R.id.tv_desc_native)
        (viewNative.bodyView as AppCompatTextView).text=ad.body


        viewNative.headlineView=baseAc.findViewById(R.id.tv_title_native)
        (viewNative.headlineView as AppCompatTextView).text=ad.headline

        viewNative.setNativeAd(ad)
        baseAc.findViewById<AppCompatImageView>(R.id.iv_cover).show(false)

        LimitManager.updateShow()
        LoadAd.removeAd(type)
        LoadAd.load(type)
        LimitManager.setRefreshStatus(type,false)
    }

    fun stopShow(){
        showJob?.cancel()
        showJob=null
    }
}