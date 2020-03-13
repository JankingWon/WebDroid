package cn.janking.webDroid

import android.app.Application
import android.content.Context
import cn.janking.webDroid.activity.CreatorActivity
import cn.janking.webDroid.event.InitFinishEvent

import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.*
import org.greenrobot.eventbus.EventBus
import java.io.File

/**
 * 自定义应用
 * @todo 部分内容改为安装时进行
 */
class MyApplication : Application() {


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        //Utils.init(this)
        if (isPreview()){
            //init()
        }else{
            //读取配置
            Config.instance.preview = false
            Config.readFromString(FileUtils.getFileContent(base.assets.open(EnvironmentUtils.DEFAULT_CONFIG_FILE)))
        }
    }

    /**
     * 判断当前APP是WebDroidCreator还是WebDroidAPP
     */
    private fun isPreview(): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(this.packageName);
        val launchComponentName = intent?.component;
        return launchComponentName?.className == CreatorActivity::class.java.name
    }

    /**
     * 初始化
     */
    private fun init() {
        if (SPUtils.getInstance().getBoolean(ConstUtils.SPKey.hasInit)) {
            EventBus.getDefault().post(InitFinishEvent(true))
            return
        }
        ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>() {
            override fun doInBackground() {
                //复制资源
                copyAssets("template")
                copyAssets("key")
                //解压apk，此项如果在debug模式有问题
                ZipUtils.unzipFile(
                    File(Utils.getApp().packageResourcePath),
                    FileUtils.getExistDir(EnvironmentUtils.dirUnzippedApk)
                )
                //删除原有签名
                FileUtils.deleteFilesInDirWithFilter(
                    EnvironmentUtils.dirUnzippedApkMetaINF
                ) { pathname ->
                    FileUtils.getFileExtension(pathname).run {
                        equals("MF") || equals("SF") || equals("RSA")
                    }
                }
                //删除原有asset
                FileUtils.deleteFilesInDirWithFilter(
                    EnvironmentUtils.dirUnzippedApkAssets
                ) { pathname ->
                    pathname.name.run {
                        !equals(EnvironmentUtils.DEFAULT_CONFIG_FILE)
                    }
                }
            }

            override fun onFail(t: Throwable?) {
                LogUtils.w("初始化错误")
                EventBus.getDefault().post(InitFinishEvent(false))
                t?.printStackTrace()
            }

            override fun onSuccess(result: Unit) {
                //写入SP中
                SPUtils.getInstance().put(ConstUtils.SPKey.hasInit, true)
                LogUtils.w("初始化完成")
                EventBus.getDefault().post(InitFinishEvent(true))
            }
        })
    }

    /**
     * 复制资源
     */
    internal fun copyAssets(assetFolder: String) {
        for (name in Utils.getApp().assets.list(assetFolder)!!) {
            FileUtils.copyFileToFile(
                this.assets.open(assetFolder + File.separator + name),
                EnvironmentUtils.getSubRoot(assetFolder + File.separator + name)
            )
        }
    }
}
