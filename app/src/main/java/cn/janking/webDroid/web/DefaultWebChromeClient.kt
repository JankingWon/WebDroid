package cn.janking.webDroid.web

import android.util.Log
import android.view.View
import com.just.agentweb.WebChromeClient

/**
 * @author Janking
 */
class DefaultWebChromeClient : WebChromeClient() {
    var mCallback: CustomViewCallback? = null

}
