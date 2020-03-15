package cn.janking.webDroid.web

import android.util.Log
import android.view.View
import android.webkit.WebChromeClient

/**
 * @author Janking
 */
class DefaultWebChromeClient : WebChromeClient() {
    var mCallback: CustomViewCallback? = null
    override fun onShowCustomView(
        view: View?,
        callback: CustomViewCallback?
    ) {

        super.onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {

        super.onHideCustomView()
    }
}
