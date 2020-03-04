package cn.janking.webDroid

import android.app.Application
import android.app.LauncherActivity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast

import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.*
import java.io.File

/**
 * 自定义应用
 * @todo 部分内容改为安装时进行
 */
class MyApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (isPreview()) {
            //预览模式不读取配置
            Config.instance.run {
                preview = true
                titles = arrayOf(
                    "微博",
                    "知乎"
                )
                urls = arrayOf(
                    "https://weibo.com",
                    "https://zhihu.com"
                )
                tabCount = 2
            }
        } else {
            //读取配置
            Config.instance.preview = false
            Config.read(FileUtils.getFileContent(base.assets.open(EnvironmentUtils.DEFAULT_CONFIG_FILE)))
        }
    }

    private fun isPreview(): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(this.packageName);
        val launchComponentName = intent?.component;
        return launchComponentName?.className == CreatorActivity::class.java.name
    }
}
