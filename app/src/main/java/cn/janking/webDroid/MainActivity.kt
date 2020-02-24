package cn.janking.webDroid

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import cn.janking.webDroid.constant.PermissionConstants
import cn.janking.webDroid.util.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOError
import java.io.IOException
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    companion object{
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

                override fun onDenied(permissionsDeniedForever: List<String>,
                                      permissionsDenied: List<String>) {
                    Log.i(TAG, "请求权限失败！")
                    finish()
                }
            })
            .request()
    }

    fun onClick(view: View) {
        when(view.id){
            R.id.preview -> preview()
            R.id.build -> build()
        }
    }

    /**
     * 预览生成的app
     */
    fun preview(){
        startActivity(Intent(this, WebActivity::class.java))
    }

    /**
     * 生成apk
     */
    fun build(){

    }
}
