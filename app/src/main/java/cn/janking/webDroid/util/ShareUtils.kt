package cn.janking.webDroid.util

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getSystemService
import cn.janking.webDroid.R
import cn.janking.webDroid.constant.PathConstants
import com.bumptech.glide.Glide
import java.io.File


/**
 * @author Janking
 */
object ShareUtils {
    /**
     * 分享 内容
     */
    fun shareMessage(message: String?) {
        message?.let {
            ActivityUtils.startActivity(
                Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, it)
                        type = "text/plain"
                    },
                    Utils.getString(R.string.msg_share_title)
                )
            )
        }
    }

    /**
     * 使用浏览器打开 url
     */
    fun openUrl(url: String?) {
        url?.let {
            ActivityUtils.startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(it)
            })
        }
    }

    /**
     * 分享图片到其他应用
     */
    fun shareImage(imageUrl: Uri?) {
        imageUrl?.let {
            ActivityUtils.startActivity(Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, it)
                type = "image/*"
            })
        }
    }

    /**
     * 复制链接到剪切板中
     */
    fun copyUrl(url: String?) {
        url.let {
            //获取剪贴板管理器：
            val cm: ClipboardManager? =
                Utils.getApp().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 创建普通字符型ClipData
            val mClipData: ClipData = ClipData.newPlainText("Label", url)
            // 将ClipData内容放到系统剪贴板里。
            cm?.run {
                primaryClip = mClipData
                Toast.makeText(Utils.getApp(), "已复制到剪切板", Toast.LENGTH_SHORT).show()
            }

        }
    }

    /**
     * 弹出全屏窗口显示图片
     */
    fun fullDialogImage(imageUrl: String?) {
        imageUrl?.let {
            Dialog(ActivityUtils.getTopActivity(), R.style.DialogFullscreen).run {
                setContentView(R.layout.dialog_fullscreen)
                val imageView: ImageView = findViewById(R.id.img_full_screen_dialog)
                //使用Glide加载图片
                Glide.with(ActivityUtils.getTopActivity()).load(it).into(imageView)
                val toolBar: Toolbar = findViewById(R.id.toolbar_full_screen_dialog)
                toolBar.setNavigationOnClickListener { dismiss() }
                show()
            }
        }

    }

    /**
     * 保存网络图片
     */
    fun saveImage(imageUrl: String?){
        ThreadUtils.executeByCached(object :
            ThreadUtils.SimpleTask<File>() {
            override fun doInBackground(): File {
                return Glide.with(ActivityUtils.getTopActivity())
                    .asFile()
                    .load(imageUrl)
                    .submit().get()
            }

            override fun onSuccess(result: File?) {
                result?.run {
                    ThreadUtils.executeByCached(object :
                        ThreadUtils.SimpleTask<Unit>() {
                        override fun doInBackground() {
                            FileUtils.copyFileToDir(result, PathConstants.dirImage, "jpeg")
                        }

                        override fun onFail(t: Throwable?) {
                            Toast.makeText(Utils.getApp(), "保存失败", Toast.LENGTH_SHORT).show()
                            LogUtils.w(t)
                        }

                        override fun onSuccess(unit : Unit) {
                            Toast.makeText(Utils.getApp(), "已保存到${PathConstants.dirImage}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        })
    }

    private fun safeCast(string: String?, function: (arg1: String) -> Unit) {
        string?.let(function)
    }
}