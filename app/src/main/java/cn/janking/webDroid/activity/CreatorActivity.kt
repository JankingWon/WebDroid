package cn.janking.webDroid.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import cn.janking.webDroid.R
import cn.janking.webDroid.adapter.ItemTouchHelperCallback
import cn.janking.webDroid.adapter.TabListRVAdapter
import cn.janking.webDroid.event.BuildFinishEvent
import cn.janking.webDroid.event.InitFinishEvent
import cn.janking.webDroid.helper.PermissionHelper
import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.*
import kotlinx.android.synthetic.main.activity_creator.*
import kotlinx.android.synthetic.main.activity_creator.drawer
import kotlinx.android.synthetic.main.activity_creator.toolbar
import kotlinx.android.synthetic.main.activity_webdroid.*
import kotlinx.android.synthetic.main.layout_nav.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * WebDroidCreator 的 主Activity
 * @author Janking
 */
open class CreatorActivity : BaseActivity() {
    /**
     * 布局Id
     */
    override val layoutId = R.layout.activity_creator
    /**
     * toolbar右边菜单id
     */
    override val toolBarMenuId: Int = R.menu.menu_creator
    /**
     * 配置tab列表的适配器
     */
    var tabListAdapter: TabListRVAdapter? = null
    /**
     * 是否正在build
     */
    var isBuilding: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        loadLastConfig()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    /**
     * 在此处保存不是绝对安全，应该在onPause
     * 但是这个不是重要数据，同时为了避免躲避多次IO，就写在了onStop
     */
    override fun onStop() {
        super.onStop()
        //保存输入的配置
        generateConfig(true)
        SPUtils.getInstance().put(Utils.getString(R.string.key_last_onfig), Config.toJsonString())
    }

    override fun initViews() {
        //tab设置
        tabListAdapter = TabListRVAdapter(this)
        tabList.layoutManager = LinearLayoutManager(this)
        tabList.adapter = tabListAdapter
        //tab滑动删除和移动的手势操作
        val callback: ItemTouchHelper.Callback = ItemTouchHelperCallback(tabListAdapter!!)
        val mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(tabList)
        //初始化状态
        if(SPUtils.getInstance().getBoolean(getString(R.string.key_has_init))){
            ConsoleUtils.success(console, "已就绪")
        }
    }

    override fun onClickViewId(viewId: Int) {
        super.onClickViewId(viewId)
        when(viewId){
            //关于
            R.id.action_menu_about -> {
                ShareUtils.openUrl("https://github.com/JankingWon/WebDroid")
            }
            //添加tab按钮
            R.id.addTab -> {
                (tabList.adapter as TabListRVAdapter).addTabItem()
            }
            //预览按钮
            R.id.preview -> {
                if (checkConfig(true)) {
                    startActivity(Intent(this@CreatorActivity, WebDroidActivity::class.java))
                }
            }
            //打包按钮
            R.id.build -> {
                //开始打包任务
                PermissionHelper.checkStorage(fun (){
                    if (checkConfig(false)) {
                        showProgressBar(true)
                        BuildUtils.build(console)
                        build.isEnabled = false
                    }
                }){}
            }
        }
    }

    /**
     * 加载上次输入的配置
     */
    private fun loadLastConfig() {
        Config.readFromString(SPUtils.getInstance().getString(Utils.getString(R.string.key_last_onfig)))
        appName.setText(Config.instance.appName)
        appPackage.setText(Config.instance.appPackage)
        for (i in 0 until Config.instance.tabCount) {
            tabListAdapter?.addTabItem(Config.instance.tabTitles[i], Config.instance.tabUrls[i])
        }
    }

    /**
     * 检查Config是否有效
     */
    private fun checkConfig(isPreview: Boolean): Boolean {
        if (checkAppName() && checkAppPackage()) {
            generateConfig(isPreview)
            return true
        }
        return false
    }

    /**
     * 生成Config参数
     */
    private fun generateConfig(isPreview: Boolean){
        Config.instance.run {
            preview = isPreview
            appName = this@CreatorActivity.appName.text.toString()
            appPackage = this@CreatorActivity.appPackage.text.toString()
            tabTitles = tabListAdapter?.tabTitleItems!!.map {
                it.toString()
            }
            tabUrls = tabListAdapter?.tabUrlItems!!.map {
                it.toString()
            }
            tabCount = tabTitles.size.coerceAtMost(tabUrls.size)
        }
    }

    /**
     * 检查app的名称
     */
    private fun checkAppName(): Boolean {
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
    private fun checkAppPackage(): Boolean {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(initFinishEvent: InitFinishEvent) {
        if (initFinishEvent.success) {
            ConsoleUtils.success(console, "已就绪")
        } else {
            ConsoleUtils.error(console, "初始化错误！")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(buildFinishEvent: BuildFinishEvent) {
        showProgressBar(false)
        //使按钮转为常规态
        build.isEnabled = true
    }
}