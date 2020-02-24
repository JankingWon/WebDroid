package cn.janking.webDroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.View
import androidx.core.graphics.ColorUtils
import cn.janking.webDroid.util.*
import cn.janking.webDroid.helper.DialogHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * 检查权限
     */
    override fun onStart() {
        super.onStart()
        PermissionUtils.permission(*PermissionUtils.getPermissions().toTypedArray())
            .rationale { shouldRequest -> DialogHelper.showRationaleDialog(shouldRequest) }
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {
                    LogUtils.i( "请求权限成功！")
                }

                override fun onDenied(
                    permissionsDeniedForever: List<String>,
                    permissionsDenied: List<String>
                ) {
                    LogUtils.i("请求权限失败！")
                }
            })
            .request()
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.preview -> preview()
            R.id.build -> build()
        }
    }

    /**
     * 预览生成的app
     */
    fun preview() {
        startActivity(Intent(this, WebActivity::class.java))
    }

    /**
     * 生成apk
     */
    fun build() {
        ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>(){
            override fun doInBackground() {
                SpanUtils.with(console)
                    .append(console.text)
                    .appendLine("开始打包...")
                    .create()
                //删除原有文件
                //复制配置
                //压缩
                SpanUtils.with(console)
                    .append(console.text)
                    .appendLine("正在压缩...")
                    .create()
                ZipUtils.zipFiles(
                    File(EnvironmentUtils.getDirUnzippedApk()).listFiles().toList(),
                    FileUtils.getExistFile(EnvironmentUtils.getFileApkUnsigned())
                )
                SpanUtils.with(console)
                    .append(console.text)
                    .appendLine("正在签名...")
                    .create()
                //签名
                SignApkUtils.main(
                    arrayOf(
                        EnvironmentUtils.getKeyPem(),
                        EnvironmentUtils.getKeyPk8(),
                        EnvironmentUtils.getFileApkUnsigned(),
                        EnvironmentUtils.getFileApkSigned()
                    )
                )
            }

            override fun onCancel() {
                SpanUtils.with(console)
                    .append(console.text)
                    .appendLine("打包取消！")
                    .setForegroundColor(cn.janking.webDroid.util.ColorUtils.getColor(R.color.rainbow_yellow))
                    .create()
            }

            override fun onFail(t: Throwable?) {
                SpanUtils.with(console)
                    .append(console.text)
                    .appendLine(String.format("打包失败！(%s)", t?.message))
                    .setForegroundColor(cn.janking.webDroid.util.ColorUtils.getColor(R.color.rainbow_red))
                    .create()
                t?.printStackTrace()
            }

            override fun onSuccess(result: Unit?) {
                SpanUtils.with(console)
                    .append(console.text)
                    .appendLine("打包完成！")
                    .setForegroundColor(cn.janking.webDroid.util.ColorUtils.getColor(R.color.loveGreen))
                    .create()
            }
        })
    }
}
