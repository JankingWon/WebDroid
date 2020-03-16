package cn.janking.webDroid.web

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import cn.janking.webDroid.R
import cn.janking.webDroid.util.LogUtils
import cn.janking.webDroid.util.ScreenUtils
import cn.janking.webDroid.web.extend.*
import cn.janking.webDroid.web.lifecycle.WebLifeCycleImpl
import cn.janking.webDroid.web.view.NestedScrollWebView

/**
 * @author Janking
 */
/**
 * 包含WebView的布局的封装类
 */
class WebBox(activity: Activity, homeUrl: String) {
    //Webview视频播放器
    val webVideoPlayer: WebVideoPlayer

    //WebView根布局
    val webLayout: FrameLayout = LayoutInflater.from(activity).inflate(
        R.layout.layout_webview, null
    ) as FrameLayout

    //错误页面
    val errorPage = webLayout.findViewById<NestedScrollView>(R.id.errorPage).apply {
        //解决错误页无法滑动的问题
        (getChildAt(0) as TextView).height = ScreenUtils.getScreenHeight()
    }

    //WebView控件
    private val webView: WebView = NestedScrollWebView(activity).apply {
        //添加布局属性
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        webLayout.addView(this)
        //使用默认setting
        defaultSetting()
        //使用默认WebViewClient
        defaultWebViewClient(this@WebBox)
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
    val webLifeCycle = WebLifeCycleImpl(webView)

    /**
     * 返回当前显示的url
     */
    fun getUrl(): String {
        return webView.url
    }

    /**
     * 记录刷新的时间
     */
    private var reloadTime = -1L

    /**
     * 刷新webView
     */
    fun reload() {
        System.currentTimeMillis().let {
            //间隔大于1s才会刷新
            if (it - reloadTime > 1000) {
                //刷新页面
                webView.reload()
                reloadTime = it
            }
        }
    }

    /**
     * 显示错误页
     */
    fun showErrorPage() {
        webView.visibility = View.INVISIBLE
        errorPage.visibility = View.VISIBLE
    }

    /**
     * 取消错误页
     */
    fun dismissErrorPage() {
        webView.visibility = View.VISIBLE
        errorPage.visibility = View.GONE
    }

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