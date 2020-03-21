package cn.janking.webDroid.web.extend

import android.os.Build
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import cn.janking.webDroid.R
import cn.janking.webDroid.constant.PathConstants
import cn.janking.webDroid.constant.WebConstants
import cn.janking.webDroid.util.LogUtils
import cn.janking.webDroid.util.Utils
import cn.janking.webDroid.util.WebUtils
import cn.janking.webDroid.web.WebConfig

/**
 * @author Janking
 */
fun WebView.defaultSetting() {
    settings.apply {
        //支持缩放，是其他设置缩放的前提
        setSupportZoom(true)
        //设置可以缩放，解决 sysu.edu.cn 不能缩放的问题
        builtInZoomControls = true
        //隐藏原生的缩放按钮
        displayZoomControls = false
        //设置文本缩放
        textZoom = 100
        //设置初始缩放比例
        //setInitialScale(100)
        //缩放至屏幕的大小
        loadWithOverviewMode = true
        //将图片调整到适合webview的大小, 解决 sysu.edu.cn 超出屏幕部分内容不加载，只有背景的问题
        useWideViewPort = true
        //根据cache-control获取数据。
        if (WebUtils.networkAvailable()) {
            //有网络时默认加载
            cacheMode = WebSettings.LOAD_DEFAULT
        } else {
            //无网络时从缓存加载
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            Toast.makeText(
                Utils.getApp(),
                Utils.getString(R.string.msg_load_cache_web_page),
                Toast.LENGTH_SHORT
            ).show()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //适配5.0不允许http和https混合使用情况, 允许混合内容
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
        //自动加载图片
        loadsImagesAutomatically = true
        //禁止多窗口
        setSupportMultipleWindows(false)
        //是否阻塞加载网络图片  协议http or https
        blockNetworkImage = false
        //设置支持javascript
        javaScriptEnabled = true
        //允许加载本地文件html  file协议
        allowFileAccess = true
        //通过 file url 加载的 Javascript 读取其他的本地文件 .建议关闭
        allowFileAccessFromFileURLs = false
        //允许通过 file url 加载的 Javascript 可以访问其他的源，包括其他的文件和 http，https 等其他的源
        allowUniversalAccessFromFileURLs = false
        javaScriptCanOpenWindowsAutomatically = true
        //支持内容重新布局
        layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        domStorageEnabled = true
        //当webview调用requestFocus时为webview设置节点
        setNeedInitialFocus(true)
        //设置编码格式
        defaultTextEncodingName = "utf-8"
        //默认字体大小
        defaultFontSize = 16
        //设置 WebView 支持的最小字体大小，默认为 8
        minimumFontSize = 12
        //允许访问位置
        setGeolocationEnabled(true)
        val dir = PathConstants.dirWebCache
        if (WebConfig.DEBUG) {
            LogUtils.i(
                "WebView.defaultSetting()",
                "dir:" + dir + "   appcache:" + PathConstants.dirWebCache
            )
        }
        //允许使用数据库
        databaseEnabled = true
        //允许使用缓存
        setAppCacheEnabled(true)
        //设置缓存路径
        setAppCachePath(dir)
        //设置用户标识
        userAgentString += WebConstants.USERAGENT_UC
        if (WebConfig.DEBUG) {
            LogUtils.i(
                "WebView.defaultSetting()",
                "UserAgentString : $userAgentString"
            )
        }
    }

}