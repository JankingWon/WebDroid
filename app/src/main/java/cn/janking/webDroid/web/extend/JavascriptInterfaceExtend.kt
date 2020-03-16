package cn.janking.webDroid.web.extend

import android.webkit.WebView

/**
 * @author Janking
 */
fun WebView.defaultJavascriptInterface() {
    addJavascriptInterface(JavascriptInterface, "Java")
}

object JavascriptInterface {
    @android.webkit.JavascriptInterface
    fun onPageFinished(success: Boolean) {
        if (success) {

        } else {

        }
    }
}