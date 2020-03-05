package cn.janking.webDroid.widget

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import cn.janking.webDroid.R

class WebDroidView constructor(
    val contentView: View,
    private val configUrl: String
) {
    /**
     * 真正的主页
     */
    var configHomeUrl: String? = null

    private var webView: WebView? = null

    companion object {
        fun createView(context: Context, viewGroup: ViewGroup, url: String): WebDroidView {
            return WebDroidView(
                LayoutInflater.from(context).inflate(
                    R.layout.layout_webdroid,
                    viewGroup,
                    false
                ), url
            ).apply {
                init()
            }
        }
    }

    fun init() {
        webView = contentView.findViewById(R.id.webView)
        webView?.apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (configHomeUrl == null) {
                        configHomeUrl = url
                    }
                    loadUrl(url)
                    return true
                }
            }
            settings.run {
                javaScriptEnabled = true
                allowFileAccess = true
                allowFileAccessFromFileURLs = true
                allowContentAccess = true
                domStorageEnabled = true
            }
            webChromeClient = object : WebChromeClient() {
                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    AlertDialog.Builder(context)
                        .setMessage(message)
                        .setPositiveButton(
                            "确定"
                        ) { dialog, _ -> dialog.dismiss() }
                        .create().show()
                    result?.confirm()
                    return true
                }
            }
            loadUrl(configUrl)
        }
    }

    fun getWebView(): WebView {
        return webView!!
    }

    /**
     * 是否处理返回按钮
     */
    fun handleBack(): Boolean {
        webView!!.run {
            if (url != configHomeUrl && canGoBack()) {
                goBack()
                return true
            }
        }
        return false
    }

}