package cn.janking.webDroid.web.extend

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.view.View
import android.webkit.*
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

    override fun onPermissionRequest(request: PermissionRequest?) {
        super.onPermissionRequest(request)
    }

    override fun onPermissionRequestCanceled(request: PermissionRequest?) {
        super.onPermissionRequestCanceled(request)
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        super.onGeolocationPermissionsShowPrompt(origin, callback)
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
    }

}