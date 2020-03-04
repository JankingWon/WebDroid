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
    val contentView: View
) {
    /**
     * 真正的主页
     */
    public var configHomeUrl: String? = null

    companion object {
        fun createView(context: Context, viewGroup: ViewGroup, url : String) : WebDroidView {
            val webDroidView = WebDroidView(
                LayoutInflater.from(context).inflate(
                    R.layout.fragment_webdroid,
                    viewGroup,
                    false
                )
            );
            //初始化webView
            webDroidView.contentView.findViewById<WebView>(R.id.webView).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        if (webDroidView.configHomeUrl == null) {
                            webDroidView.configHomeUrl = url
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
                loadUrl(url)
            }
            return webDroidView
        }
    }
}