package cn.janking.webDroid.widget

import android.app.Activity
import android.content.Context
import android.view.*
import android.view.ViewGroup.LayoutParams
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager
import cn.janking.webDroid.R
import com.just.agentweb.AgentWeb
import com.just.agentweb.IWebLayout
import com.just.agentweb.WebViewClient


class WebDroidItem constructor(
    context: Context,
    viewGroup: ViewGroup,
    configUrl: String
) {

    val agentWeb = AgentWeb.with(context as Activity)
        .setAgentWebParent(
            viewGroup, LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )
        .closeIndicator()
        .setWebLayout(AgentWebLayout(context, viewGroup))
        .createAgentWeb()
        .ready()
        .go(configUrl)

    fun handleKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return agentWeb.handleKeyEvent(keyCode, event)
    }
}

class AgentWebLayout(context: Context, viewGroup: ViewGroup) : IWebLayout<WebView, FrameLayout> {
    val contentView: View = LayoutInflater.from(context).inflate(
        R.layout.layout_webview,
        viewGroup,
        false
    )

    override fun getLayout(): FrameLayout {
        return contentView as FrameLayout
    }

    override fun getWebView(): WebView? {
        return contentView.findViewById<WebView>(R.id.webView)
    }
}