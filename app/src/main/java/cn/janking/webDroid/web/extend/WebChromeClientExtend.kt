package cn.janking.webDroid.web.extend

import android.app.Activity
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import cn.janking.webDroid.util.LogUtils
import cn.janking.webDroid.web.WebVideoPlayer

/**
 * @author Janking
 */
fun WebView.defaultWebChromeClient(webVideoPlayer: WebVideoPlayer) {
    webChromeClient = DefaultWebChromeClient(webVideoPlayer)
}


class DefaultWebChromeClient(private val webVideoPlayer: WebVideoPlayer?) : WebChromeClient() {

    /**
     * 重载自定义页面，如播放视频
     */
    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        webVideoPlayer?.onShowCustomView(view, callback)
    }

    /**
     * 重载结束自定义页面，如播放视频
     */
    override fun onHideCustomView() {
        webVideoPlayer?.onHideCustomView()
    }

    /**
     * 重载JS的console输出
     */
    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        LogUtils.i("JS", consoleMessage?.message())
        return true
    }

}