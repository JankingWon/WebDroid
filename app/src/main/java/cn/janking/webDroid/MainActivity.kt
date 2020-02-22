package cn.janking.webDroid

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.*
import cn.janking.webDroid.util.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object{
        val TAG = "MainActivity";
    }
    private var configUrl : String? = null

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        ConfigUtil.context = newBase
        configUrl = PropertiesUtil.getStringByKey( this,"url")
        ThreadUtil.execute {
            FileUtil.copyAssets(newBase, "template")
            FileUtil.copyAssets(newBase, "tool")
        }
        val packageManager = packageManager
        val allPackages = packageManager.getInstalledPackages(0)
        for (i in allPackages.indices) {
            val packageInfo = allPackages[i]
            val path = packageInfo.applicationInfo.sourceDir
            val name = packageInfo.applicationInfo.loadLabel(packageManager).toString()
            Log.i(TAG, path)
            Log.i(TAG, name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
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
                AlertDialog.Builder(this@MainActivity)
                        .setMessage(message)
                        .setPositiveButton("确定"
                        ) { dialog, _ -> dialog.dismiss() }
                        .create().show()
                result?.confirm()
                return true
            }
        }

        webView.loadUrl(configUrl)
    }

    override fun onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack()
        }else{
            webView.loadUrl(configUrl)
        }
    }

    fun onClick(view: View) {
        when(view.id){
            R.id.build -> build()
        }
    }

    fun build(){
        Runtime.getRuntime().exec(
            arrayOf(
                "chmod",
                "744",
                ConfigUtil.getFileAPT()
            )
        )
        Log.d(TAG, BuildUtil.runshell(arrayOf(
            ConfigUtil.getFileAPT(),
            "package",
            "--auto-add-overlay",
            "-m",
            "-J",
            getExternalFilesDir("gen" + File.separator)?.absolutePath,
            "-J",
            ConfigUtil.getFileAndroid(),
            "-T",
            ConfigUtil.getPathGen(),
            "-M",
            ConfigUtil.getFileManifest(),
            "-S",
            ConfigUtil.getPathRes()
        )))
    }
}
