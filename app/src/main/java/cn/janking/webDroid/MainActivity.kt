package cn.janking.webDroid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.janking.webDroid.helper.DialogHelper
import cn.janking.webDroid.util.*
import kotlinx.android.synthetic.main.activity_main.*

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
        BuildUtils.requestStoragePermission()
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.preview -> preview()
            R.id.build -> BuildUtils.build(console)
        }
    }

    /**
     * 预览生成的app
     */
    fun preview() {
        startActivity(Intent(this, WebActivity::class.java))
    }
}
