package cn.janking.webDroid.layout

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import cn.janking.webDroid.R
import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.ConsoleUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Janking
 */
class EditAppLayout(activity: Activity) : EditLayout() {
    /**
     * 视图
     */
    override val contentView = LayoutInflater.from(activity)
        .inflate(R.layout.layout_edit_app, null) as LinearLayout
    /**
     * app名称
     */
    val appName = contentView.findViewById<EditText>(R.id.appName)
    /**
     * app包名
     */
    val appPackage = contentView.findViewById<EditText>(R.id.appPackage)

    init {
        loadLastConfig()
        appName.addTextChangedListener(object : TextWatcher {
            var lastAutoFillText = appPackage.text.toString()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (appPackage.text.isNullOrBlank() or (appPackage.text.toString() == lastAutoFillText)) {
                    lastAutoFillText = "cn.janking.$s"
                    appPackage.setText(lastAutoFillText)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun loadLastConfig() {
        appName.setText(Config.instance.appName)
        appPackage.setText(Config.instance.appPackage)
    }

    override fun generateConfig() {
        Config.instance.let {
            it.appName = appName.text.toString()
            it.appPackage = appPackage.text.toString()
        }
    }


    /**
     * 检查app的名称
     */
    fun checkAppName(console: TextView): Boolean {
        if (appName.text.isNullOrEmpty()) {
            ConsoleUtils.warning(console, "APP名称必填！")
            return false
        } else if (appName.text.toString().length >= 9) {
            ConsoleUtils.warning(console, "APP名称最多为8个字符！")
        }
        return true
    }


    /**
     * 检查app的包名
     */
    fun checkAppPackage(console: TextView): Boolean {
        if (appPackage.text.isNullOrEmpty()) {
            ConsoleUtils.warning(console, "APP包名必填！")
            return false
        }
        val tempPackage = appPackage.text.toString()
        // Java/Android合法包名，可以包含大写字母、小写字母、数字和下划线，用点(英文句号)分隔称为段，且至少包含2个段，隔开的每一段都必须以字母开头
        val pattern: Pattern =
            Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)+([.][a-zA-Z_][a-zA-Z0-9_]*)+([.][a-zA-Z_][a-zA-Z0-9_]*)")
        val matcher: Matcher = pattern.matcher(tempPackage)
        if (!matcher.matches()) {
            ConsoleUtils.warning(
                console,
                "APP包名不合法！(示例: cn.janking.webDroid)"
            )
            return false
        }
        return true
    }


}