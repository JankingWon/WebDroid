package cn.janking.webDroid.layout

import android.app.Activity
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import cn.janking.webDroid.R
import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.*
import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Janking
 */
class EditAppLayout(activity: Activity) : EditLayout() {
    companion object {
        const val SELECT_FILE_REQUEST_CODE = 200
    }

    /**
     * 视图
     */
    override val contentView = LayoutInflater.from(activity)
        .inflate(R.layout.layout_edit_app, null) as ConstraintLayout

    /**
     * app包名
     */
    val appPackage = contentView.findViewById<EditText>(R.id.appPackage)

    /**
     * app名称
     */
    val appName = contentView.findViewById<EditText>(R.id.appName).apply {
        addTextChangedListener(object : TextWatcher {
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

    /**
     * app图标
     */
    val appIcon = contentView.findViewById<ImageView>(R.id.appIcon).apply {
        setOnClickListener {
            OpenUtils.toSelectFile("image/*", SELECT_FILE_REQUEST_CODE)
        }
        //长按预览
        setOnLongClickListener {
            return@setOnLongClickListener OpenUtils.showFullImageDialog(Config.instance.appIcon)
        }
    }

    /**
     * 版本名
     */
    val versionName = contentView.findViewById<EditText>(R.id.versionName).apply {
        setText(AppUtils.getAppVersionName())
    }

    /**
     * 版本码
     */
    val versionCode = contentView.findViewById<EditText>(R.id.versionCode).apply {
        setText(AppUtils.getAppVersionCode().toString())
    }

    /**
     * 是否允许打开其他应用
     */
    val allowOpenApp = contentView.findViewById<Spinner>(R.id.allowOpenApp)

    init {
        loadLastConfig()
    }

    /**
     * 加载上次配置
     */
    override fun loadLastConfig() {
        appName.setText(Config.instance.appName)
        appPackage.setText(Config.instance.appPackage)
        //设置默认icon
        appIcon.setImageResource(R.drawable.ic_launcher)
        loadAppIcon()
    }

    /**
     * 设置APP图标预览
     */
    fun loadAppIcon() {
        //加载config的icon
        File(Config.instance.appIcon).run {
            if (FileUtils.isFileExists(this)) {
                appIcon.setImageURI(UriUtils.file2Uri(this))
            }
        }
    }

    /**
     * 根据用户设置生成配置实例
     */
    override fun generateConfig() {
        Config.instance.let {
            it.appName = appName.text.toString()
            it.appPackage = appPackage.text.toString()
            it.versionName = versionName.text.toString()
            it.versionCode = versionCode.text.toString().toInt()
            it.allowOpenApp = allowOpenApp.selectedItemPosition
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
                "APP包名格式错误！(示例: cn.janking.webDroid)"
            )
            return false
        }
        return true
    }


    /**
     * 检查app的版本名
     */
    fun checkAppVersionName(console: TextView): Boolean {
        val tempVersionName = versionName.text.toString()
        val pattern: Pattern =
            Pattern.compile("^([0-9]+)+([.][0-9]+)+([.][0-9]+)?")
        val matcher: Matcher = pattern.matcher(tempVersionName)
        if (!matcher.matches()) {
            ConsoleUtils.warning(
                console,
                "APP版本名格式错误！(示例: 0.0.1)"
            )
            return false
        }
        return true
    }


    /**
     * 选择图标返回
     */
    fun onSelectImageResult(uri: Uri) {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<String>() {
            override fun doInBackground(): String {
                return FileUtils.copyUriToTempFile(uri).toString()
            }

            override fun onSuccess(result: String) {
                Config.instance.appIcon = result
                Utils.runOnUiThread {
                    loadAppIcon()
                }
            }

        })
    }

}