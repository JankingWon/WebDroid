package cn.janking.webDroid.util

import android.widget.TextView
import android.widget.Toast
import cn.janking.webDroid.R
import cn.janking.webDroid.constant.PermissionConstants
import cn.janking.webDroid.helper.DialogHelper
import cn.janking.webDroid.model.Config
import java.io.File
import java.lang.RuntimeException

class BuildUtils{
    companion object{
        var hasInit : Boolean = false
        /**
         * 请求读写文件权限
         */
        fun requestStoragePermission(){
            PermissionUtils.permission(PermissionConstants.STORAGE)
                .rationale { shouldRequest -> DialogHelper.showRationaleDialog(shouldRequest) }
                .callback(object : PermissionUtils.FullCallback {
                    override fun onGranted(permissionsGranted: List<String>) {
                        //获取权限后才进行初始化
                        init()
                        LogUtils.i( "请求权限成功！")
                    }

                    override fun onDenied(
                        permissionsDeniedForever: List<String>,
                        permissionsDenied: List<String>
                    ) {
                        LogUtils.i("请求权限失败！")
                        //如果选择了“拒绝后不再询问”，则引导打开权限设置页面
                        if (permissionsDeniedForever.isNotEmpty()) {
                            DialogHelper.showOpenAppSettingDialog()
                            return
                        }
                    }
                })
                .request()
        }
        /**
         * 初始化
         */
        fun init(){
            if(hasInit) return
            ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>(){
                override fun doInBackground() {
                    //复制资源和
                    copyAssets("template")
                    copyAssets("key")
                    //解压apk
                    ZipUtils.unzipFile(File(Utils.getApp().packageResourcePath), FileUtils.getExistDir(EnvironmentUtils.getDirUnzippedApk()))
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
                    //复制Manifests
                    /*  FileUtils.copyFileToDir(
                        EnvironmentUtils.getFileTemplateSub(EnvironmentUtils.DEFAULT_MANIFEST_FILE),
                        EnvironmentUtils.getDirUnzippedApk()
                    )*/
                }

                override fun onFail(t: Throwable?) {
                    LogUtils.w( "初始化错误")
                    t?.printStackTrace()
                }

                override fun onSuccess(result: Unit) {
                    hasInit = true
                    LogUtils.w( "初始化完成")
                    Utils.runOnUiThread {
                        Toast.makeText(Utils.getApp(), "初始化完成", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        /**
         * 复制资源
         */
        internal fun copyAssets(assetFolder: String) {
            for (name in Utils.getApp().assets.list(assetFolder)!!) {
                FileUtils.copyFileToFile(
                    Utils.getApp().assets.open(assetFolder + File.separator + name),
                    EnvironmentUtils.getDirRootSub(assetFolder + File.separator + name)
                )
            }
        }

        /**
         * 生成apk
         * @todo 字节对齐
         */
        fun build(console: TextView) {
            //如果没有初始化成功，则中断
            if(!hasInit){
                SpanUtils.with(console)
                    .append(console.text)
                    .appendLine("数据未初始化...")
                    .setForegroundColor(ColorUtils.getColor(R.color.rainbow_red))
                    .create()
                //重新请求权限，尝试初始化
                requestStoragePermission()
                return
            }
            ThreadUtils.executeByCached(object : ThreadUtils.SimpleTask<Unit>(){
                override fun doInBackground() {
                    SpanUtils.with(console)
                        .append(console.text)
                        .appendLine("开始打包...")
                        .create()
                    //写入配置
                    FileUtils.copyFileToDir(
                        EnvironmentUtils.getFileTemplateSub(EnvironmentUtils.DEFAULT_CONFIG_FILE),
                        EnvironmentUtils.getDirUnzippedApkAssets()
                    )
                    //压缩
                    SpanUtils.with(console)
                        .append(console.text)
                        .appendLine("正在压缩...")
                        .create()
                        ZipUtils.zipFiles(
                            File(EnvironmentUtils.getDirUnzippedApk()).listFiles().toList(),
                            FileUtils.getExistFile(EnvironmentUtils.getFileApkUnsigned())
                        )
                        SpanUtils.with(console)
                            .append(console.text)
                            .appendLine("正在签名...")
                            .create()
                        if(!FileUtils.isFileExists(EnvironmentUtils.getKeyPem())
                            || !FileUtils.isFileExists(EnvironmentUtils.getKeyPk8()) ){
                            throw RuntimeException("key is null")
                    }
                    //签名
                    SignApkUtils.main(
                        arrayOf(
                            EnvironmentUtils.getKeyPem(),
                            EnvironmentUtils.getKeyPk8(),
                            EnvironmentUtils.getFileApkUnsigned(),
                            EnvironmentUtils.getFileApkSigned()
                        )
                    )
                }

                override fun onCancel() {
                    SpanUtils.with(console)
                        .append(console.text)
                        .appendLine("打包取消！")
                        .setForegroundColor(ColorUtils.getColor(R.color.rainbow_yellow))
                        .create()
                }

                override fun onFail(t: Throwable?) {
                    SpanUtils.with(console)
                        .append(console.text)
                        .appendLine(String.format("打包失败！(%s)", t?.message))
                        .setForegroundColor(ColorUtils.getColor(R.color.rainbow_red))
                        .create()
                    t?.printStackTrace()
                }

                override fun onSuccess(result: Unit?) {
                    SpanUtils.with(console)
                        .append(console.text)
                        .appendLine("打包完成！")
                        .setForegroundColor(ColorUtils.getColor(R.color.loveGreen))
                        .create()
                }
            })
        }
    }
}