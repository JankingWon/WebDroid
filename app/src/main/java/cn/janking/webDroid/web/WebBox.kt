package cn.janking.webDroid.web

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import cn.janking.webDroid.R
import cn.janking.webDroid.web.extend.defaultOnLongClickListener
import cn.janking.webDroid.web.extend.defaultSetting
import cn.janking.webDroid.web.extend.defaultWebChromeClient
import cn.janking.webDroid.web.extend.defaultWebViewClient
import cn.janking.webDroid.web.lifecycle.WebLifeCycleImpl
/**
 * @author Janking
 */
/**
 * 包含WebView的布局的封装类
 */
class WebBox(activity: Activity, viewGroup: ViewGroup, homeUrl: String) {
    //Webview视频播放器
    val webVideoPlayer: WebVideoPlayer
    //WebView根布局
    val webLayout: View = LayoutInflater.from(activity).inflate(
        R.layout.layout_webview,
        viewGroup,
        false
    )
    //WebView控件
    val webView = webLayout.findViewById<WebView>(R.id.webView).apply {
        //使用默认setting
        defaultSetting()
        //使用默认WebViewClient
        defaultWebViewClient()
        //使用默认WebChromeClient
        webVideoPlayer = WebVideoPlayer(activity, this)
        defaultWebChromeClient(webVideoPlayer)
        //拦截长按事件
        defaultOnLongClickListener()
        //加载url
        loadUrl(homeUrl)
    }
    //Webview生命周期监控
    val webLifeCycle =
        WebLifeCycleImpl(webView)

    /**
     * 返回键的监听
     */
    fun onBack(): Boolean {
        return webView?.let {
            if (webVideoPlayer.backEvent()) {
                true
            } else if (it.canGoBack()) {
                it.goBack()
                false
            } else {
                false
            }
        } ?: false
    }
}