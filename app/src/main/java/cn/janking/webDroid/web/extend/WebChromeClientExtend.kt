package cn.janking.webDroid.web.extend

import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.*
import cn.janking.webDroid.R
import cn.janking.webDroid.util.*
import cn.janking.webDroid.web.FilePathChooserCallback
import cn.janking.webDroid.web.WebConfig
import cn.janking.webDroid.web.WebVideoPlayer

/**
 * @author Janking
 */
fun WebView.defaultWebChromeClient(webVideoPlayer: WebVideoPlayer): FilePathChooserCallback {
    return DefaultWebChromeClient(webVideoPlayer).also {
        webChromeClient = it
    }
}


class DefaultWebChromeClient(private val webVideoPlayer: WebVideoPlayer?) : WebChromeClient(),
    FilePathChooserCallback {

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

    /**
     * 重载询问位置权限的对话框
     */
    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        DialogUtils.showAlertDialog(
            Utils.getString(R.string.msg_request_geo_permission, origin),
            Runnable { callback?.invoke(origin, true, false) },
            Runnable { callback?.invoke(origin, false, true) }
        )
    }

    var filePathCallback: ValueCallback<Array<Uri>>? = null

    /**
     * 重载选择文件的操作
     */
    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        this.filePathCallback = filePathCallback
        //先自动创建Intent
        fileChooserParams?.let {
            it.createIntent()?.run {
                if (it.mode == FileChooserParams.MODE_OPEN_MULTIPLE) {
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                if (action == Intent.ACTION_GET_CONTENT) {
                    action == Intent.ACTION_OPEN_DOCUMENT
                }
                ActivityUtils.startActivityForResult(
                    ActivityUtils.getTopActivity(),
                    this,
                    WebConfig.SELECT_FILE_REQUEST_CODE
                )
                return true
            }
            //创建失败的话默认选择所有类型
            OpenUtils.toSelectFile("*/*", WebConfig.SELECT_FILE_REQUEST_CODE)
        }
        return true
    }

    /**
     * 选择文件返回的回调
     */
    override fun onChooseFile(uris: Array<Uri>) {
        filePathCallback?.onReceiveValue(uris)
    }

}