package cn.janking.webDroid

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import cn.janking.webDroid.model.Config
import kotlinx.android.synthetic.main.activity_web.*

class WebActivity : AppCompatActivity() {

    private var configHomeUrl : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        //初始化webView
        webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if(configHomeUrl == null){
                    configHomeUrl = url
                }
                view!!.loadUrl(url)
                return true
            }
        }
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowContentAccess = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = object : WebChromeClient(){
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

        webView.loadUrl(Config.getInstance().url)
    }

    /**
     * 监听返回键
     */
    override fun onBackPressed() {
        if(Config.getInstance().debug){
            super.onBackPressed()
            return
        }
        if(webView.url != configHomeUrl && webView.canGoBack()){
            webView.goBack()
        }else{
            moveTaskToBack(false);
        }
    }

}
