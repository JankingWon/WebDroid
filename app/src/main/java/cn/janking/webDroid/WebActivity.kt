package cn.janking.webDroid

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.AppUtils

class WebActivity : AppCompatActivity() {
    /**
     * 真正的主页
     */
    private var configHomeUrl : String? = null
    /**
     * 真正的主页
     */
    private var webView : WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createContentView())
    }

    private fun createContentView(): View {
        val contentView = LinearLayout(this).apply{
            orientation = LinearLayout.VERTICAL
        }
        //初始化webView
        webView = WebView(this).apply {
            webViewClient = object : WebViewClient(){
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if(configHomeUrl == null){
                        configHomeUrl = url
                    }
                    view!!.loadUrl(url)
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
            webChromeClient = object : WebChromeClient(){
                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?
                ): Boolean {
                    AlertDialog.Builder(this@WebActivity)
                        .setMessage(message)
                        .setPositiveButton("确定"
                        ) { dialog, _ -> dialog.dismiss() }
                        .create().show()
                    result?.confirm()
                    return true
                }
            }
            loadUrl(Config.getInstance().url)
        }
        return contentView.apply {
            addView(webView)
        }
    }

    /**
     * 监听返回键
     */
    override fun onBackPressed() {
        if(Config.getInstance().debug){
            super.onBackPressed()
            return
        }
        webView?.run {
            if(url != configHomeUrl && canGoBack()){
                goBack()
            }
        }?: moveTaskToBack(false);
    }
}
