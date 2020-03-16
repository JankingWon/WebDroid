package cn.janking.webDroid.web

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import cn.janking.webDroid.R
import cn.janking.webDroid.web.extend.*
import cn.janking.webDroid.web.lifecycle.WebLifeCycleImpl
import cn.janking.webDroid.web.view.NestedScrollWebView

/**
 * @author Janking
 */
/**
 * 包含WebView的布局的封装类
 */
class WebBox(activity: Activity, viewGroup: ViewGroup, homeUrl: String) {
    //Webview视频播放器
    val webVideoPlayer: WebVideoPlayer

    //WebView控件
    val webView: WebView = NestedScrollWebView(activity).apply {
        //使用默认setting
        defaultSetting()
        //使用默认WebViewClient
        defaultWebViewClient()
        //使用默认WebChromeClient
        defaultWebChromeClient(WebVideoPlayer(activity, this).also { webVideoPlayer = it })
        //使用默认下载器
        defaultDownloadListener()
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
    fun handleKeyEvent(): Boolean {
        return webView.let {
            when {
                webVideoPlayer.handleKeyEvent() -> {
                    true
                }
                it.canGoBack() -> {
                    it.goBack()
                    false
                }
                else -> {
                    false
                }
            }
        }
    }
}