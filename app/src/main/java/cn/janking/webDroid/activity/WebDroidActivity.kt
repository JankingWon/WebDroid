package cn.janking.webDroid.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.view.*
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import cn.janking.webDroid.R
import cn.janking.webDroid.adapter.BasicPagerAdapter
import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.FileUtils
import cn.janking.webDroid.util.OpenUtils
import cn.janking.webDroid.util.UriUtils
import cn.janking.webDroid.util.Utils
import cn.janking.webDroid.web.WebBox
import cn.janking.webDroid.web.WebConfig
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
    private val pageMap: MutableMap<Int, WebBox> = HashMap()

    /**
     * 滑动页面的适配器
     */
    private val pagerAdapter: PagerAdapter = object : BasicPagerAdapter() {
        override fun getCount(): Int {
            return Config.instance.tabCount
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            if (pageMap[position] == null) {
                pageMap[position] = WebBox(
                    this@WebDroidActivity,
                    Config.instance.tabUrls[position]
                )
            }
            val view = pageMap[position]!!.webLayout
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
            viewPager.currentItem = item.itemId
            for (i in 0 until bottomNavigation.menu.size()) {
                pageMap[i]?.webLifeCycle?.onPause()
            }
            pageMap[item.itemId]?.webLifeCycle?.onResume()
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
                        getCurrentWebBox().reload()
                        //下一次不算
                        clickTime = -1
                    } else {
                        clickTime = it
                    }
                }
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
                    getCurrentWebBox().reload()
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
            pageMap[tab?.position]?.webLifeCycle?.onResume()
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            pageMap[tab?.position]?.webLifeCycle?.onPause()
        }
    }

    /**
     * 适用于底部导航栏的对viewPager的监听器
     */
    private val onPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {

        override fun onPageSelected(position: Int) {
            if (bottomNavigation.selectedItemId != position) {
                bottomNavigation.selectedItemId = position
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
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
                OpenUtils.openUrl(getCurrentWebBox().getUrl())
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
                getCurrentWebBox().fileChooserCallback(arrayOf(it))
            }
        }
    }

    /**
     * 处理按键事件
     */
    override fun handleKeyEvent(keyCode: Int, event: KeyEvent?): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK && getCurrentWebBox().handleKeyEvent()
    }

    /**
     * 调用WebView的生命周期
     */
    override fun onResume() {
        for (webDroidItem in pageMap.values) {
            webDroidItem.webLifeCycle.onResume()
        }
        super.onResume()
    }

    /**
     * 调用WebView的生命周期
     */
    override fun onPause() {
        for (webDroidItem in pageMap.values) {
            webDroidItem.webLifeCycle.onPause()
        }
        super.onPause()
    }

    /**
     * 销毁WebView
     */
    override fun onDestroy() {
        for (webDroidItem in pageMap.values) {
            webDroidItem.webLifeCycle.onDestroy()
        }
        super.onDestroy()
    }

    /**
     * 获取当前tab的webBox
     */
    private fun getCurrentWebBox(): WebBox {
        return pageMap[viewPager.currentItem]!!
    }

    override fun initToolBarTitle() {
        toolbar.title = Config.instance.appName
    }

    /**
     * 初始化View
     */
    override fun initViews() {
        super.initViews()
        viewPager.adapter = pagerAdapter
        if (Config.instance.tabCount <= 1) {
            topNavigation.visibility = View.GONE
            bottomNavigation.visibility = View.GONE
        } else {
            //防止频繁回收
            viewPager.offscreenPageLimit = Config.instance.tabCount - 1
            //如果是设置顶部tab
            if (Config.instance.tabStyle == 0) {
                topNavigation.visibility = View.VISIBLE
                bottomNavigation.visibility = View.GONE
                topNavigation.setupWithViewPager(viewPager)
                topNavigation.addOnTabSelectedListener(onTabSelectedListener)
            } else {
                //设置底部tab
                topNavigation.visibility = View.GONE
                bottomNavigation.visibility = View.VISIBLE
                viewPager.addOnPageChangeListener(onPageChangeListener)
                //添加Tab
                for (i in 0 until Config.instance.tabCount) {
                    bottomNavigation.menu.add(Menu.NONE, i, i, Config.instance.tabTitles[i])
                    //添加Icon
                    bottomNavigation.menu.getItem(i).icon = Utils.getApp().resources.run {
                        if (Config.instance.preview && FileUtils.isFileExists(Config.instance.tabIcons[i])) {
                            Drawable.createFromStream(
                                contentResolver.openInputStream(
                                    UriUtils.file2Uri(File(Config.instance.tabIcons[i]))
                                ), null
                            )
                        } else {
                            getDrawable(getIdentifier("ic_tab_$i", "drawable", packageName))
                        }
                    }
                }
                //添加监听器
                bottomNavigation.setOnNavigationItemSelectedListener(
                    onNavigationItemSelectedListener
                )
                bottomNavigation.setOnNavigationItemReselectedListener(
                    onNavigationItemReselectedListener
                )
                //如果是底部tab，则需要添加底部padding，防止内容遮挡
                if (Config.instance.tabStyle == 1) {
                    viewPager.setPadding(
                        viewPager.paddingLeft,
                        viewPager.paddingTop,
                        viewPager.paddingRight,
                        bottomNavigation.measuredHeight
                    )
                }
            }
        }
    }

}