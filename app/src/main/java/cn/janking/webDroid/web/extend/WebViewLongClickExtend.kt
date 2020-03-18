package cn.janking.webDroid.web.extend

import android.app.AlertDialog
import android.webkit.WebView
import cn.janking.webDroid.util.LogUtils
import cn.janking.webDroid.util.OpenUtils

/**
 * @author Janking
 */
fun WebView.defaultOnLongClickListener(){
    setOnLongClickListener {
        val result =
            (it as WebView).hitTestResult ?: return@setOnLongClickListener false
        val type = result.type
        if (type == WebView.HitTestResult.UNKNOWN_TYPE) return@setOnLongClickListener false

        // 这里可以拦截很多类型
        when (type) {
            WebView.HitTestResult.IMAGE_TYPE,
            WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                // 获取图片的路径
                val imageUrl = result.extra
                // 使用Dialog弹出菜单
                AlertDialog.Builder(getContext())
                    .setTitle("图片选项")
                    .setItems(arrayOf("查看图片", "复制链接", "保存图片", "分享图片")) { _, which ->
                        when (which) {
                            0 -> {
                                OpenUtils.showFullImageDialog(imageUrl)
                            }
                            1 -> {
                                OpenUtils.copyUrl(imageUrl)
                            }
                            2 -> {
                                OpenUtils.saveImage(imageUrl)
                            }
                            3 -> {
                                OpenUtils.shareImage(imageUrl)
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