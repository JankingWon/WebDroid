package cn.janking.webDroid

import android.app.Application
import android.content.Context
import android.util.Log

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
        ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>(){
            override fun doInBackground() {
                Config.read(FileUtils.getFileContent(base.assets.open(EnvironmentUtils.DEFAULT_CONFIG_FILE)))
            }

            override fun onFail(t: Throwable?) {
                Log.w(MainActivity.TAG, "config读取出错")
                t?.printStackTrace()
            }

            override fun onSuccess(result: Unit) {
                Log.i(MainActivity.TAG, "config读取成功")
            }
        })

        //复制资源和apk
        ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>(){
            override fun doInBackground() {
                copyAssets("template")
                copyAssets("key")
            }

            override fun onSuccess(result: Unit) {
                Log.i(MainActivity.TAG, "asset复制成功")
            }

            override fun onFail(t: Throwable?) {
                Log.w(MainActivity.TAG, "asset复制出错")
                t?.printStackTrace()
            }
        })
        ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>(){
            override fun doInBackground(): Unit {
                ZipUtils.unzipFile(File(packageResourcePath), FileUtils.getExistDir(EnvironmentUtils.getDirUnzippedApk()))
            }

            override fun onFail(t: Throwable?) {
                Log.w(MainActivity.TAG, "apk解压出错")
                t?.printStackTrace()
            }

            override fun onSuccess(result: Unit) {
                Log.i(MainActivity.TAG, "apk解压成功")
            }
        })
    }

    /**
     * 复制资源
     */
    fun copyAssets(assetFolder: String) {
        for (name in this.assets.list(assetFolder)!!) {
            FileUtils.copyFileToFile(
                this.assets.open(assetFolder + File.separator + name),
                EnvironmentUtils.getDirRootSub(assetFolder + File.separator + name)
            )
        }
    }
}
