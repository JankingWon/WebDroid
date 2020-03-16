package cn.janking.webDroid.web.extend

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.*
import androidx.annotation.RequiresApi
import cn.janking.webDroid.R
import cn.janking.webDroid.constant.WebConstants
import cn.janking.webDroid.util.*
import cn.janking.webDroid.web.WebBox
import cn.janking.webDroid.web.WebConfig
import java.util.*

/**
 * @author Janking
 */
fun WebView.defaultWebViewClient(webBox: WebBox) {
    webViewClient = DefaultWebClient(webBox).also {
        addJavascriptInterface(it, "Java")
    }

}

class DefaultWebClient(val webBox: WebBox) : WebViewClient() {
    /**
     * 缓存当前出现错误的页面
     */
    private val errorPageSet = HashSet<String>()
    /**
     * 缓存等待加载完成的页面 onPageStart()执行之后 ，onPageFinished()执行之前
     */
    private val cachePageSet = HashSet<String>()
    private val DOC_CALLBACK = "if (document.URL.indexOf('data:text/html') == -1) {" +
            "      window.Java.onPageFinished(true);\n" +
            "} else {\n" +
            "      window.Java.onPageFinished(false);\n" +
            "}"

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
            if (WebConfig.DEBUG) {
                LogUtils.i("determineOpenApp", url)
            }
            return true
        }
        /**
         * 拦截未知协议
         */
        if (WebConfig.interceptUnknownUrl) {
            if (WebConfig.DEBUG) {
                LogUtils.i("Intercept Unknown Url :" + request.url)
            }
            return true
        }
        return super.shouldOverrideUrlLoading(view, request)
    }

    /**
     * 处理打开其他应用的情况
     * 已安装，提示打开
     */
    private fun determineOpenApp(uri: Uri): Boolean {
        return OpenUtils.getResolveInfoFromUri(uri)?.let {
            when (WebConfig.handleOpenUrl) {
                //直接跳转
                WebConfig.DIRECT_OPEN_OTHER_PAGE -> {
                    return openApp(uri)
                }
                //询问用户
                WebConfig.ASK_USER_OPEN_OTHER_PAGE -> {
                    DialogUtils.showAlertDialog(
                        Utils.getApp().resources.getString(
                            R.string.msg_open_app,
                            AppUtils.getAppName(),
                            it.loadLabel(Utils.getApp().packageManager)
                        ),
                        Runnable {
                            openApp(uri)
                        }
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
     * 开始加载回调
     */
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        //网络不可用时显示错误页面
        if (!WebUtils.checkNetwork(Utils.getApp())) {
            webBox.showNetworkErrorPage()
        } else {
            webBox.dismissErrorPage()
            super.onPageStarted(view, url, favicon)
        }
    }

    /**
     * 加载完成回调
     */
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
    }

    /**
     * 错误回调 Android6.0以下
     */
    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        webBox.showErrorPage()
        if (WebConfig.DEBUG) {
            LogUtils.i(
                "onReceivedError:${description}",
                "url:${failingUrl}",
                "code:${errorCode}"
            )
        }
        super.onReceivedError(view, errorCode, description, failingUrl)
    }

    /**
     * 错误回调 Android6.0以上
     */
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        val url = request.url.toString()
        //只处理mainFrame的错误，忽略iframe等
        if (request.isForMainFrame) {
            webBox.showErrorPage()
        }
        if (WebConfig.DEBUG) {
            LogUtils.i(
                "onReceivedError:${error.description}",
                "url:${url}",
                "code:${error.errorCode}"
            )
        }
        super.onReceivedError(view, request, error)
    }


    /**
     * 缩放回调
     */
    override fun onScaleChanged(
        view: WebView,
        oldScale: Float,
        newScale: Float
    ) {
        if (WebConfig.DEBUG) {
            LogUtils.i(
                "onScaleChanged:$oldScale",
                "newScale:$newScale"
            )
        }
        if (newScale - oldScale > WebConfig.abnormalScale) {
            view.setInitialScale((oldScale / newScale * 100).toInt())
        }
        super.onScaleChanged(view, oldScale, newScale)
    }
}
