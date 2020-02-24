package cn.janking.webDroid

import android.app.Application
import android.content.Context
import android.util.Log

import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.*
import java.io.File
import java.io.FileFilter

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
                LogUtils.w( "config读取出错")
                t?.printStackTrace()
            }

            override fun onSuccess(result: Unit) {
                LogUtils.w( "config读取成功")
            }
        })

        //复制资源和apk
        ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>(){
            override fun doInBackground() {
                copyAssets("template")
                copyAssets("key")
            }

            override fun onSuccess(result: Unit) {
                LogUtils.i("asset复制成功")
            }

            override fun onFail(t: Throwable?) {
                LogUtils.w("asset复制出错")
                t?.printStackTrace()
            }
        })
        ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>(){
            override fun doInBackground(): Unit {
                //解压apk
                ZipUtils.unzipFile(File(packageResourcePath), FileUtils.getExistDir(EnvironmentUtils.getDirUnzippedApk()))
                //删除原有签名
                FileUtils.deleteFilesInDirWithFilter(EnvironmentUtils.getDirUnzippedApkMetaINF()
                ) { pathname -> FileUtils.getFileExtension(pathname).run {
                        equals("MF") || equals("SF") || equals("RSA")
                } }
                //删除原有asset
                FileUtils.deleteFilesInDirWithFilter(EnvironmentUtils.getDirUnzippedApkAssets()
                ) { pathname -> pathname.name.run {
                    !equals(EnvironmentUtils.DEFAULT_CONFIG_FILE)
                } }
            }

            override fun onFail(t: Throwable?) {
                LogUtils.w("apk解压出错")
                t?.printStackTrace()
            }

            override fun onSuccess(result: Unit) {
                LogUtils.i( "apk解压成功")
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
