package cn.janking.webDroid.web

import android.app.Activity
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import cn.janking.webDroid.R
import cn.janking.webDroid.helper.PermissionHelper
import cn.janking.webDroid.util.*
import com.bumptech.glide.Glide
import com.just.agentweb.AgentWeb
import com.just.agentweb.AgentWebConfig
import com.just.agentweb.IWebLayout
import java.io.File


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
        .setWebChromeClient(DefaultWebChromeClient())
        .createAgentWeb()
        .ready()
        .go(configUrl).also {
            it.agentWebSettings.webSettings.run {
                //解决 sysu.edu.cn 超出屏幕部分内容不加载，只有背景的问题
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
        //拦截长按事件
        setOnLongClickListener {
            val result =
                (it as WebView).hitTestResult ?: return@setOnLongClickListener false
            val type = result.type
            if (type == WebView.HitTestResult.UNKNOWN_TYPE) return@setOnLongClickListener false

            // 这里可以拦截很多类型
            when (type) {
                WebView.HitTestResult.IMAGE_TYPE -> {
                    // 获取图片的路径
                    val imageUrl = result.extra
                    // 使用Dialog弹出菜单
                    AlertDialog.Builder(getContext())
                        .setTitle("图片选项")
                        .setItems(arrayOf("查看图片", "复制链接", "保存图片", "分享图片")) { _, which ->
                            when (which) {
                                0 -> {
                                    ShareUtils.fullDialogImage(imageUrl)
                                }
                                1 -> {
                                    ShareUtils.copyUrl(imageUrl)
                                }
                                2 -> {
                                    PermissionHelper.checkStorage(fun (){
                                        ShareUtils.saveImage(imageUrl)
                                    }){}

                                }
                                3 -> {
                                    //下载完成之后再分享
                                    ThreadUtils.executeByCached(object :
                                        ThreadUtils.SimpleTask<File>() {
                                        override fun doInBackground(): File {
                                            return Glide.with(getContext())
                                                .asFile()
                                                .load(imageUrl)
                                                .submit().get()
                                        }

                                        override fun onSuccess(result: File?) {
                                            result?.run {
                                                ShareUtils.shareImage(UriUtils.file2Uri(this))
                                            }
                                        }
                                    })

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