package cn.janking.webDroid

import android.app.Application
import android.content.Context
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
        //读取配置
        Config.read(FileUtils.getFileContent(base.assets.open(EnvironmentUtils.DEFAULT_CONFIG_FILE)))
    }
}
