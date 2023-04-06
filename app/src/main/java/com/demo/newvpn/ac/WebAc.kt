package com.demo.newvpn.ac

import com.demo.newvpn.R
import com.demo.newvpn.base.BaseAc
import com.demo.newvpn.config.Local
import kotlinx.android.synthetic.main.activity_web.*


class WebAc : BaseAc() {
    override fun layout(): Int = R.layout.activity_web

    override fun initView() {
        immersionBar.statusBarView(top).init()
        iv_back.setOnClickListener { finish() }
        webview.apply {
            settings.javaScriptEnabled=true
            loadUrl(Local.URL)
        }
    }
}