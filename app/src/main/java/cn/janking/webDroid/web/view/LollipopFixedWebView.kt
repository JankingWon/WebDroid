package cn.janking.webDroid.web.view

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView

/**
 * 修复 Android 5.0 & 5.1 打开 WebView 闪退问题：
 * 参阅 https://stackoverflow.com/questions/41025200/android-view-inflateexception-error-inflating-class-android-webkit-webview
 */
open class LollipopFixedWebView : WebView {

    constructor(
        context: Context
    ):super(getFixedContext(context))

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(getFixedContext(context), attrs)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr : Int
    ) : super(getFixedContext(context), attrs, defStyleAttr)


    companion object {
        fun getFixedContext(context: Context): Context {
            return if (Build.VERSION.SDK_INT in 21..22) { // Avoid crashing on Android 5 and 6 (API level 21 to 23)
                context.createConfigurationContext(Configuration())
            } else context
        }
    }
}