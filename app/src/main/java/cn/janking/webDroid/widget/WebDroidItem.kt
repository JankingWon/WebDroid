package cn.janking.webDroid.widget

import android.app.Activity
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import cn.janking.webDroid.R
import cn.janking.webDroid.util.LogUtils
import cn.janking.webDroid.util.ShareUtils
import cn.janking.webDroid.util.ThreadUtils
import cn.janking.webDroid.util.Utils
import com.bumptech.glide.Glide
import com.just.agentweb.AgentWeb
import com.just.agentweb.AgentWebConfig
import com.just.agentweb.IWebLayout


/**
 * 内部嵌入了AgentWeb，方便管理
 */
class WebDroidItem constructor(
    context: Context,
    viewGroup: ViewGroup,
    configUrl: String
) {
    init {
        //设置debug
        AgentWebConfig.debug()
    }

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
        .go(configUrl).also {
            it.agentWebSettings.webSettings.run {
                //解决 sysu.edu.cn 部分内容不加载，只有背景的问题
                useWideViewPort = true
                //解决 sysu.edu.cn 不能缩放的问题
                builtInZoomControls = true
                //隐藏原生的缩放按钮
                displayZoomControls = false;
            }
        }

    fun handleKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return agentWeb.handleKeyEvent(keyCode, event)
    }
}

/**
 * WebView的布局
 */
class AgentWebLayout(context: Context, viewGroup: ViewGroup) : IWebLayout<WebView, FrameLayout> {
    //根布局
    private val contentView: View = LayoutInflater.from(context).inflate(
        R.layout.layout_webview,
        viewGroup,
        false
    )
    //WebView控件
    private val webView = contentView.findViewById<WebView>(R.id.webView).apply {
        setOnLongClickListener {
            val result =
                (it as WebView).hitTestResult ?: return@setOnLongClickListener false
            val type = result.type
            if (type == WebView.HitTestResult.UNKNOWN_TYPE) return@setOnLongClickListener false

            // 这里可以拦截很多类型，我们只处理图片类型就可以了
            when (type) {
                WebView.HitTestResult.IMAGE_TYPE -> {
                    // 获取图片的路径
                    val saveImgUrl = result.extra
                    // 使用Dialog弹出菜单
                    AlertDialog.Builder(getContext())
                        .setTitle("图片选项")
                        .setItems(arrayOf("查看图片", "复制链接", "下载图片", "分享图片")) { dialog, which ->
                            when (which) {
                                0 -> {
                                    ShareUtils.fullDialogImage(saveImgUrl)
                                }
                                1 -> {
                                    ShareUtils.copyUrl(saveImgUrl)
                                }
                                2 -> {
                                    Glide.with(context)
                                        .asFile()
                                        .load(saveImgUrl)
                                        .submit()
                                }
                                3 -> {
                                    ShareUtils.shareImage(saveImgUrl)
                                }
                            }
                        }
                        .show()
                }
                else -> LogUtils.d("else")
            }
            true
        }
    }

    override fun getLayout(): FrameLayout {
        return contentView as FrameLayout
    }

    override fun getWebView(): WebView? {
        return webView
    }
}