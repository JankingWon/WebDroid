/*
 * Copyright (C)  Justson(https://github.com/Justson/AgentWeb)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.janking.webDroid.web.lifecycle

import android.os.Looper
import android.view.ViewGroup
import android.webkit.WebView

/**
 * @author cenxiaozhong
 * @date 2017/6/3
 * @since 2.0.0
 */
class WebLifeCycleImpl internal constructor(private val mWebView: WebView?) :
    WebLifeCycle {
    override fun onResume() {
        mWebView?.run {
            onResume()
            resumeTimers()
        }
    }

    override fun onPause() {
        mWebView?.run {
            onPause()
            pauseTimers()
        }
    }

    override fun onDestroy() {
        mWebView?.resumeTimers()
        clearWebView(
            mWebView
        )
    }

    companion object {
        fun clearWebView(w: WebView?) {
            val webView: WebView = w ?: return
            if (Looper.myLooper() != Looper.getMainLooper()) {
                return
            }
            webView.loadUrl("about:blank")
            webView.stopLoading()
            if (webView.handler != null) {
                webView.handler.removeCallbacksAndMessages(null)
            }
            webView.removeAllViews()
            val mViewGroup: ViewGroup = webView.parent as ViewGroup
            mViewGroup.removeView(webView)
            webView.webChromeClient = null
            webView.webViewClient = null
            webView.tag = null
            webView.clearHistory()
            webView.destroy()
        }
    }

}