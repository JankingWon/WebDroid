package cn.janking.webDroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import cn.janking.webDroid.util.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = "MainActivity";
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * 检查权限
     */
    override fun onResume() {
        super.onResume()
        PermissionUtils.permission(*PermissionUtils.getPermissions().toTypedArray())
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: List<String>) {
                    Log.i(TAG, "请求权限成功！")
                }

                override fun onDenied(
                    permissionsDeniedForever: List<String>,
                    permissionsDenied: List<String>
                ) {
                    Log.i(TAG, "请求权限失败！")
                    finish()
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
        //删除原有文件
        //复制配置
        //压缩
        Log.i(TAG, "正在压缩...")
        ZipUtils.zipFiles(
            File(EnvironmentUtils.getDirUnzippedApk()).listFiles().toList(),
            FileUtils.getExistFile(EnvironmentUtils.getFileApkUnsigned())
        )
        Log.i(TAG, "正在签名...")
        //签名
        SignApkUtils.main(
            arrayOf(
                EnvironmentUtils.getKeyPem(),
                EnvironmentUtils.getKeyPk8(),
                EnvironmentUtils.getFileApkUnsigned(),
                EnvironmentUtils.getFileApkSigned()
                )
        )
        Log.i(TAG, "打包完成")
    }
}
