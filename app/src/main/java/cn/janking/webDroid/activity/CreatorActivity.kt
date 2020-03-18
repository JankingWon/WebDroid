package cn.janking.webDroid.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import cn.janking.webDroid.R
import cn.janking.webDroid.adapter.BasicPagerAdapter
import cn.janking.webDroid.event.BuildFinishEvent
import cn.janking.webDroid.event.InitFinishEvent
import cn.janking.webDroid.helper.PermissionHelper
import cn.janking.webDroid.layout.EditAppLayout
import cn.janking.webDroid.layout.EditLayout
import cn.janking.webDroid.layout.EditTabLayout
import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.*
import kotlinx.android.synthetic.main.activity_creator.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

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
     * Sub Layout
     */
    var editAppLayout: EditAppLayout? = null
    var editTabLayout: EditTabLayout? = null
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
        SPUtils.getInstance().put(Utils.getString(R.string.key_last_config), Config.toJsonString())
    }


    /**
     * 滑动页面的适配器
     */
    private val pagerAdapter: PagerAdapter = object : BasicPagerAdapter() {
        override fun getCount(): Int {
            return 2
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val editLayout: EditLayout = if (position == 0) {
                EditAppLayout(this@CreatorActivity).apply {
                    editAppLayout = this
                }
            } else {
                EditTabLayout(this@CreatorActivity).apply {
                    editTabLayout = this
                }
            }
            return editLayout.run {
                container.addView(
                    contentView,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                contentView
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "APP"
                else -> "TAB"
            }
        }
    }


    override fun initToolBarTitle() {
        toolbar.title = AppUtils.getAppName()
    }

    override fun initViews() {
        //初始化状态
        if (SPUtils.getInstance().getBoolean(getString(R.string.key_has_init))) {
            ConsoleUtils.success(console, "已就绪")
        }
        //设置tab
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 1
        topNavigation.setupWithViewPager(viewPager)
    }

    override fun onClickViewId(viewId: Int) {
        super.onClickViewId(viewId)
        when (viewId) {
            //关于
            R.id.action_menu_about -> {
                OpenUtils.openUrl("https://github.com/JankingWon/WebDroid")
            }
            //添加tab按钮
/*            R.id.addTab -> {
                (tabList.adapter as TabListRVAdapter).addTabItem()
            }*/
            //预览按钮
            R.id.preview -> {
                if (checkConfig(true)) {
                    startActivity(Intent(this@CreatorActivity, WebDroidActivity::class.java))
                }
            }
            //打包按钮
            R.id.build -> {
                //开始打包任务
                PermissionHelper.checkStorage(Runnable {
                    if (checkConfig(false)) {
                        showProgressBar(true)
                        BuildUtils.build(console)
                        build.isEnabled = false
                    }
                })
            }
            //输出区域
            R.id.console -> {
                //安装APK
                if (console.text.contains("打包完成！")) {
                    BuildUtils.install()
                } else {
                    //显示完整内容
                    DialogUtils.showMessageDialog(console.text)
                }

            }
        }
    }

    /**
     * 加载上次输入的配置
     */
    private fun loadLastConfig() {
        Config.readFromString(SPUtils.getInstance().getString(Utils.getString(R.string.key_last_config)))
    }

    /**
     * 检查Config是否有效
     */
    private fun checkConfig(isPreview: Boolean): Boolean {
        if (checkAppConfig()) {
            generateConfig(isPreview)
            return true
        }
        return false
    }

    private fun checkAppConfig(): Boolean {
        return editAppLayout?.let {
            it.checkAppName(console) && it.checkAppPackage(console)
        } ?: false
    }

    /**
     * 生成Config参数
     */
    private fun generateConfig(isPreview: Boolean) {
        Config.instance.preview = isPreview
        editAppLayout?.generateConfig()
        editTabLayout?.generateConfig()
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