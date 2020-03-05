package cn.janking.webDroid

import android.content.Intent
import android.os.Bundle
import android.view.View
import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.BuildUtils
import cn.janking.webDroid.util.ConsoleUtils
import cn.janking.webDroid.util.EnvironmentUtils
import cn.janking.webDroid.util.FileUtils
import kotlinx.android.synthetic.main.activity_creator.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.regex.Matcher
import java.util.regex.Pattern


class CreatorActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creator)
        EventBus.getDefault().register(this)
        initViews()
    }

    /**
     * 检查权限
     */
    override fun onStart() {
        super.onStart()
        BuildUtils.requestStoragePermission()
        if(BuildUtils.hasInit){
            ConsoleUtils.success(console,"已就绪" )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        preview.setOnClickListener {
            if (generateProperty(true)) {
                startActivity(Intent(this@CreatorActivity, WebDroidActivity::class.java))
            }
        }
        build.setOnClickListener {
            if (generateProperty(false)) {
                showProgressBar(true)
                BuildUtils.build(console)
            }
        }

    }

    /**
     * 生成Config参数
     */
    private fun generateProperty(isPreview: Boolean): Boolean {
        if (checkAppName() && checkAppPackage()) {
            Config.instance.run {
                preview = isPreview
                appName = this@CreatorActivity.appName.text.toString()
                appPackage = this@CreatorActivity.appPackage.text.toString()
                titles = arrayOf(
                    "微博",
                    "知乎"
                )
                urls = arrayOf(
                    "https://weibo.com",
                    "https://zhihu.com"
                )
                tabCount = 2
                tabStyle = 1
            }
            return true
        }
        return false
    }

    /**
     * 检查app的名称
     */
    private fun checkAppName(): Boolean {
        if(appName.text.isNullOrEmpty()){
            ConsoleUtils.warning(console, "APP名称必填！")
            return false
        }else if(appName.text.toString().length >= 9){
            ConsoleUtils.warning(console, "APP名称最多为8个字符！")
        }
        return true
    }


    /**
     * 检查app的包名
     */
    private fun checkAppPackage(): Boolean {
        if(appPackage.text.isNullOrEmpty()){
            ConsoleUtils.warning(console, "APP包名必填！")
            return false
        }
        val tempPackage = appPackage.text.toString()
        // Java/Android合法包名，可以包含大写字母、小写字母、数字和下划线，用点(英文句号)分隔称为段，且至少包含2个段，隔开的每一段都必须以字母开头
        val pattern: Pattern =
            Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)+([.][a-zA-Z_][a-zA-Z0-9_]*)+([.][a-zA-Z_][a-zA-Z0-9_]*)")
        val matcher: Matcher = pattern.matcher(tempPackage)
        if(!matcher.matches()){
            ConsoleUtils.warning(
                console,
                "APP包名不合法！(示例: cn.janking.webDroid)"
            )
            return false
        }
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(initFinish: InitFinish){
        if(initFinish.success){
            ConsoleUtils.success(console,"已就绪" )
        }else{
            ConsoleUtils.error(console,"初始化错误！" )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(buildFinishEvent: BuildFinishEvent){
        showProgressBar(false)
    }

    /**
     * 控制等待条的显隐
     */
    private fun showProgressBar(show: Boolean) {
        if (show) {
            spaceLine.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        } else {
            spaceLine.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
        }
    }
}

/**
 * 用于传递打包过程结束的信息
 */
class BuildFinishEvent

/**
 * 用于传达初始化完成的信息
 */
class InitFinish(val success:Boolean)