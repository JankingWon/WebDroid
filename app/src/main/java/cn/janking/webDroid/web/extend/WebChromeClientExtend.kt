package cn.janking.webDroid.web.extend

import android.app.Activity
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import cn.janking.webDroid.web.WebVideoPlayer

/**
 * @author Janking
 */
fun WebView.defaultWebChromeClient(webVideoPlayer: WebVideoPlayer) {
    webChromeClient = DefaultWebChromeClient(webVideoPlayer)
}


class DefaultWebChromeClient(val webVideoPlayer: WebVideoPlayer?) : WebChromeClient() {

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        webVideoPlayer?.onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        webVideoPlayer?.onHideCustomView()
    }
}