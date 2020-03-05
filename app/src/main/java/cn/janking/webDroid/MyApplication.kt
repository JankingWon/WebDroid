package cn.janking.webDroid

import android.app.Application
import android.content.Context

import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.*

/**
 * 自定义应用
 * @todo 部分内容改为安装时进行
 */
class MyApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (!isPreview()){
            //读取配置
            Config.instance.preview = false
            Config.readFromString(FileUtils.getFileContent(base.assets.open(EnvironmentUtils.DEFAULT_CONFIG_FILE)))
        }
    }

    private fun isPreview(): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(this.packageName);
        val launchComponentName = intent?.component;
        return launchComponentName?.className == CreatorActivity::class.java.name
    }
}
