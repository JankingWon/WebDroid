package cn.janking.webDroid.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import androidx.viewpager.widget.PagerAdapter
import cn.janking.webDroid.R
import cn.janking.webDroid.adapter.BasicPagerAdapter
import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.*
import cn.janking.webDroid.web.WebBox
import cn.janking.webDroid.web.WebConfig
import cn.janking.webDroid.web.WebFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_webdroid.*
import java.io.File


/**
 * 生成的WebDridAPP的 主Activity
 * @author Janking
 */
class WebDroidActivity : BaseActivity() {
    /**
     * 布局Id
     */
    override val layoutId = R.layout.activity_webdroid

    /**
     * toolbar右边菜单id
     */
    override val toolBarMenuId: Int = R.menu.menu_webdroid

    /**
     * 缓存页面
     */
    private val webBoxMap: MutableMap<Int, WebBox> = HashMap()

    /**
     * 滑动页面的适配器
     */
    private val pagerAdapter: PagerAdapter = object : BasicPagerAdapter() {
        override fun getCount(): Int {
            return Config.instance.tabCount
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            if (webBoxMap[position] == null) {
                webBoxMap[position] = WebBox(
                    Utils.getApp(),
                    this@WebDroidActivity,
                    Config.instance.tabUrls[position]
                )
            }
            val view = webBoxMap[position]!!.webLayout
            //如果已经被回收了，需要手动添加进去
            if (container.indexOfChild(view) == -1) {
                container.addView(
                    view,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
            return view
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return Config.instance.tabTitles[position]
        }
    }

    /**
     * 底部导航栏的监听器
     */
    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            showFragment(bottomNavigation.childCount - item.order)
            return@OnNavigationItemSelectedListener true
        }
    private val onNavigationItemReselectedListener =
        object : BottomNavigationView.OnNavigationItemReselectedListener {
            var clickTime: Long = -1
            override fun onNavigationItemReselected(item: MenuItem) {
                //0.5s以内算双击
                System.currentTimeMillis().let {
                    if (it - clickTime < 500) {
                        //刷新
                        getCurrentWebBox()?.reload()
                        //下一次不算
                        clickTime = -1
                    } else {
                        clickTime = it
                    }
                }
            }
        }

    /**
     * @param i 从左往右第几个TAB
     * 用于底部tab栏切换fragment
     */
    private fun showFragment(i: Int) {
        supportFragmentManager.let {
            it.beginTransaction().apply {
                it.fragments.forEachIndexed { index, value ->
                    if (index == i) {
                        show(value)
                    } else {
                        hide(value)
                    }
                }
            }.commitAllowingStateLoss()
        }
    }

    /**
     * 顶部导航栏的监听器
     */
    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        var clickTime: Long = -1

        //双击tab刷新
        override fun onTabReselected(tab: TabLayout.Tab?) {
            //0.5s以内算双击
            System.currentTimeMillis().let {
                if (it - clickTime < 500) {
                    //刷新
                    getCurrentWebBox()?.reload()
                    //下一次不算
                    clickTime = -1
                } else {
                    clickTime = it
                }
            }

        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            clickTime = System.currentTimeMillis()
            //此处不能使用getCurrentWebView() 因为 viewPager.currentItem 还未更改
            webBoxMap.get(tab?.position)?.webLifeCycle?.onResume()
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            webBoxMap.get(tab?.position)?.webLifeCycle?.onPause()
        }
    }

    /**
     * 配置更改时调用
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            }
        }
    }

    /**
     * 重写点击事件
     */
    override fun onClickViewId(viewId: Int) {
        super.onClickViewId(viewId)
        when (viewId) {
            //调用浏览器
            R.id.action_menu_browser -> {
                OpenUtils.openUrl(getCurrentWebBox()?.getUrl())
            }
            //分享
            R.id.action_menu_share -> {
                OpenUtils.shareMessage(Utils.getString(R.string.msg_share))
            }
        }
    }

    /**
     * 选择文件返回
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WebConfig.SELECT_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                getCurrentWebBox()?.fileChooserCallback(arrayOf(it))
            }
        }
    }

    /**
     * 处理按键事件
     */
    override fun handleKeyEvent(keyCode: Int, event: KeyEvent?): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK && getCurrentWebBox()?.handleKeyEvent() ?: false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置数据目录
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 安卓9.0后不允许多进程使用同一个数据目录，需设置前缀来区分
            val processName = Utils.getCurrentProcessName()
            if (AppUtils.getAppPackageName() != processName) {
                try {
                    WebView.setDataDirectorySuffix(processName)
                } catch (exception: IllegalStateException) {
                    //忽略
                }
            }
        }
        //设置WebConfig
        WebConfig.handleOpenUrl = Config.instance.allowOpenApp
    }

    /**
     * 调用WebView的生命周期
     */
    override fun onResume() {
        if (Config.instance.tabStyle == 0) {
            getCurrentWebBox()?.webLifeCycle?.onResume()
        } else {
            for (element in webBoxMap) {
                element.value.webLifeCycle.onResume()
            }
        }
        super.onResume()
    }

    /**
     * 调用WebView的生命周期 @todo bottom tab实现点击tab其他tab onPause
     */
    override fun onPause() {
        if (Config.instance.tabStyle == 0) {
            getCurrentWebBox()?.webLifeCycle?.onPause()
        } else {
            for (element in webBoxMap) {
                element.value.webLifeCycle.onPause()
            }
        }
        super.onPause()
    }

    /**
     * 保存tab位置
     */
    override fun onStop() {
        super.onStop()
    }

    /**
     * 销毁WebView
     */
    override fun onDestroy() {
        for (webBox in webBoxMap.values) {
            webBox.webLifeCycle.onDestroy()
        }
        super.onDestroy()
    }

    /**
     * @return 从左往右数第几个tab
     * 获取当前页面的index
     */
    private fun getCurrentIndex(): Int {
        return when {
            Config.instance.tabCount <= 1 -> {
                0
            }
            Config.instance.tabStyle == 0 -> {
                viewPager.currentItem
            }
            else -> {
                bottomNavigation.selectedItemId % 10000
            }
        }
    }

    /**
     * 获取当前tab的webBox
     */
    private fun getCurrentWebBox(): WebBox? {
        return webBoxMap[getCurrentIndex()]
    }

    override fun initToolBarTitle() {
        toolbar.title = Config.instance.appName
    }

    /**
     * 初始化View
     */
    override fun initViews() {
        super.initViews()
        //不显示tab栏
        if (Config.instance.tabCount <= 1) {
            topNavigation.visibility = View.GONE
            bottomNavigation.visibility = View.GONE
        } else {
            //防止频繁回收
            viewPager.offscreenPageLimit = Config.instance.tabCount - 1
            //如果是设置顶部tab
            if (Config.instance.tabStyle == 0) {
                topNavigation.visibility = View.VISIBLE
                viewPager.visibility = View.VISIBLE
                bottomNavigation.visibility = View.GONE
                fragmentContainer.visibility = View.GONE
                viewPager.adapter = pagerAdapter
                topNavigation.setupWithViewPager(viewPager)
                topNavigation.addOnTabSelectedListener(onTabSelectedListener)
            } else {
                //设置底部tab
                topNavigation.visibility = View.GONE
                viewPager.visibility = View.GONE
                bottomNavigation.visibility = View.VISIBLE
                fragmentContainer.visibility = View.VISIBLE
                //添加Tab
                for (i in 0 until Config.instance.tabCount) {
                    //添加menu Item，其id设置为自定义
                    bottomNavigation.menu.add(Menu.NONE, 10000 + i, i, Config.instance.tabTitles[i])
                    //添加Icon
                    bottomNavigation.menu.getItem(i).icon = Utils.getApp().resources.run {
                        if (Config.instance.preview && FileUtils.isFileExists(Config.instance.tabIcons[i])) {
                            Drawable.createFromStream(
                                contentResolver.openInputStream(
                                    UriUtils.file2Uri(File(Config.instance.tabIcons[i]))
                                ), null
                            )
                        } else {
                            //这里需要用"cn.janking.webDroid"，因为打包后apk里R没有修改，仍然是之前的包名
                            getDrawable(
                                getIdentifier(
                                    "ic_tab_$i",
                                    "drawable",
                                    "cn.janking.webDroid"
                                )
                            )
                        }
                    }
                }
                //添加视图
                supportFragmentManager.beginTransaction().apply {
                    //逆序添加，使第一个页面首先显示
                    for (i in Config.instance.tabCount - 1 downTo 0 ){
                        webBoxMap[i] = WebBox(
                            Utils.getApp(),
                            this@WebDroidActivity,
                            Config.instance.tabUrls[i]
                        )
                        add(R.id.fragmentContainer, WebFragment(webBoxMap[i]!!))
                    }
                }.commitAllowingStateLoss()

                //添加监听器
                bottomNavigation.setOnNavigationItemSelectedListener(
                    onNavigationItemSelectedListener
                )
                bottomNavigation.setOnNavigationItemReselectedListener(
                    onNavigationItemReselectedListener
                )
                //如果是底部tab，则需要添加底部padding，防止内容遮挡
                //注意：这个padding是toolbar的高度，而不是底部tab的高度，因为协调布局会隐藏顶部toolbar
                toolbar.post {
                    fragmentContainer.setPadding(
                        viewPager.paddingLeft,
                        viewPager.paddingTop,
                        viewPager.paddingRight,
                        toolbar.measuredHeight
                    )
                }
            }
        }
    }

}