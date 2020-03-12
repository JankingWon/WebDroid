package cn.janking.webDroid.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import cn.janking.webDroid.R
import cn.janking.webDroid.adapter.ItemTouchHelperCallback
import cn.janking.webDroid.adapter.TabListRVAdapter
import cn.janking.webDroid.event.BuildFinishEvent
import cn.janking.webDroid.event.CancelBuildEvent
import cn.janking.webDroid.event.InitFinishEvent
import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.BuildUtils
import cn.janking.webDroid.util.ConsoleUtils
import cn.janking.webDroid.util.ConstUtils
import cn.janking.webDroid.util.SPUtils
import kotlinx.android.synthetic.main.activity_creator.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.regex.Matcher
import java.util.regex.Pattern


class CreatorActivity : BaseActivity() {
    var tabListAdapter: TabListRVAdapter? = null
    /**
     * 是否正在build
     */
    var isBuilding: Boolean = false

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
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun initViews() {
        setSupportActionBar(toolbar)
        //tab设置
        tabListAdapter = TabListRVAdapter(this)
        tabList.layoutManager = LinearLayoutManager(this)
        tabList.adapter = tabListAdapter
        //tab滑动删除和移动的手势操作
        val callback: ItemTouchHelper.Callback = ItemTouchHelperCallback(tabListAdapter!!)
        val mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(tabList)
        //添加tab按钮
        addTab.setOnClickListener {
            (tabList.adapter as TabListRVAdapter).addTabItem()
        }
        //预览按钮
        preview.setOnClickListener {
            if (generateProperty(true)) {
                startActivity(Intent(this@CreatorActivity, WebDroidActivity::class.java))
            }
        }
        //打包按钮
        build.setOnClickListener {
            if (isBuilding) {
                //取消打包任务
                EventBus.getDefault().post(CancelBuildEvent())
                build.text = "打包"
                isBuilding = false
            } else {
                //开始打包任务
                if (generateProperty(false)) {
                    showProgressBar(true)
                    BuildUtils.build(console)
                    isBuilding = true
                    build.text = "取消"
                }
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
                tabTitles = tabListAdapter?.tabTitleItems!!.map {
                    it.toString()
                }
                tabUrls = tabListAdapter?.tabUrlItems!!.map {
                    it.toString()
                }
                tabCount = tabTitles.size.coerceAtMost(tabUrls.size)
            }
            return true
        }
        return false
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