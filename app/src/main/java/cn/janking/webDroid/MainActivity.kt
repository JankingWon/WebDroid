package cn.janking.webDroid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginBottom
import cn.janking.webDroid.util.BuildUtils

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
            height = 300
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
