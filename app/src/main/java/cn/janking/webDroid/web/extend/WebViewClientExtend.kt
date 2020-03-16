package cn.janking.webDroid.web.extend

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Message
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import cn.janking.webDroid.constant.WebConstants
import cn.janking.webDroid.helper.DialogHelper
import cn.janking.webDroid.util.*
import cn.janking.webDroid.web.WebConfig

/**
 * @author Janking
 */
fun WebView.defaultWebViewClient() {
    webViewClient = DefaultWebClient()
}

class DefaultWebClient : WebViewClient() {
    /**
     * 重载URL
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest
    ): Boolean {
        val url = request.url.toString()
        when (request.url.scheme) {
            /**
             * 普通http/s
             */
            WebConstants.HTTP_SCHEME,
            WebConstants.HTTPS_SCHEME -> {
                view.loadUrl(url)
                return true
            }
            /**
             * 电话、短信、邮件、位置
             */
            WebView.SCHEME_TEL,
            WebConstants.SCHEME_SMS,
            WebView.SCHEME_MAILTO,
            WebView.SCHEME_GEO -> {
                return ActivityUtils.startActivity(Uri.parse(url))
            }
        }

        /**
         * 其他应用跳转协议，需要查询是否存在应用
         */
        if (determineOpenApp(request.url)) {
            if(WebConfig.DEBUG){
                LogUtils.i("determineOpenApp", url)
            }
            return true
        }
        /**
         * 拦截未知协议
         */
        if (WebConfig.interceptUnknownUrl) {
            if(WebConfig.DEBUG){
                LogUtils.i("intercept UnkownUrl :" + request.url)
            }
            return true
        }
        return super.shouldOverrideUrlLoading(view, request)
    }

    /**
     * 获取调用APP对话框的回调
     */
    private fun getDialogCallBack(uri: Uri): Handler.Callback {
        return object : Handler.Callback {
            override fun handleMessage(msg: Message): Boolean {
                when (msg.what) {
                    1 -> openApp(uri)
                    else -> return true
                }
                return true
            }
        }
    }

    /**
     * 处理打开其他应用的情况
     * 已安装，提示打开
     */
    private fun determineOpenApp(uri: Uri): Boolean {
        return OpenUtils.getResolveInfoFromUri(uri)?.let {
            when (WebConfig.handleOpenUrl) {
                WebConfig.DIRECT_OPEN_OTHER_PAGE -> {
                    return openApp(uri)
                }
                WebConfig.ASK_USER_OPEN_OTHER_PAGE -> {
                    DialogHelper.showOpenApp(
                        getDialogCallBack(uri),
                        it.loadLabel(Utils.getApp().packageManager).toString()
                    )
                    return true
                }
                else -> false
            }
        } ?: false
    }

    /**
     * 跳到该应用
     */
    private fun openApp(uri: Uri): Boolean {
        return ActivityUtils.startActivity(uri, Intent.URI_INTENT_SCHEME)
    }

    /**
     * MainFrame Error
     *
     * @param view
     * @param errorCode
     * @param description
     * @param failingUrl
     */
    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
    ) {
        if (WebConfig.DEBUG) {
            LogUtils.i(
                "onReceivedError：$description",
                "CODE:$errorCode"
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        if (WebConfig.DEBUG){
            LogUtils.i(
                "onReceivedError:${error.description}",
                "code:${error.errorCode}"
            )
        }
    }


    override fun onScaleChanged(
        view: WebView,
        oldScale: Float,
        newScale: Float
    ) {
        if(WebConfig.DEBUG){
            LogUtils.i(
                "onScaleChanged:$oldScale",
                "newScale:$newScale"
            )
        }
        if (newScale - oldScale > WebConfig.abnormalScale) {
            view.setInitialScale((oldScale / newScale * 100).toInt())
        }
    }
}
