package cn.janking.webDroid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginBottom
import androidx.core.view.marginStart
import cn.janking.webDroid.util.BuildUtils
import cn.janking.webDroid.util.SizeUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createContentView())
    }

    private fun createContentView():View{
        val contentView = LinearLayout(this).apply{
            orientation = LinearLayout.VERTICAL
        }
        val console = TextView(this).apply {
            width = LinearLayout.LayoutParams.MATCH_PARENT
            height = 0
            isVerticalScrollBarEnabled = true
        }
        val preview = Button(this).apply {
            text = "Preview"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, WebActivity::class.java))
            }
        }
        val build = Button(this).apply {
            text = "Build"
            setOnClickListener{
                BuildUtils.build(console)
            }
        }
        return contentView.apply {
            addView(preview)
            addView(build)
            addView(console)
            (console.layoutParams as LinearLayout.LayoutParams).run {
                weight = 1f
                marginStart = SizeUtils.dp2px(10f)
            }
        }
    }

    /**
     * 检查权限
     */
    override fun onStart() {
        super.onStart()
        BuildUtils.requestStoragePermission()
    }
}
