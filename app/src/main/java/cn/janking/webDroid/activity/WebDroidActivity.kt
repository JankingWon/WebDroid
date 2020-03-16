package cn.janking.webDroid.activity

import android.content.res.Configuration
import android.view.*
import android.webkit.WebView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import cn.janking.webDroid.R
import cn.janking.webDroid.model.Config
import cn.janking.webDroid.util.OpenUtils
import cn.janking.webDroid.util.Utils
import cn.janking.webDroid.web.WebBox
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_webdroid.*


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
    private val pagerAdapter: PagerAdapter = object : PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

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
            if (viewPager.indexOfChild(view) == -1) {
                viewPager.addView(
                    view,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
            return view
        }

        override fun destroyItem(
            container: ViewGroup,
            position: Int,
            `object`: Any
        ) {
            container.removeView(`object` as View)
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
            return@OnNavigationItemSelectedListener true
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
                if(it - clickTime < 500){
                    //刷新
                    getCurrentWebBox().reload()
                    //下一次不算
                    clickTime = -1
                }else{
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

    private fun getCurrentWebBox(): WebBox {
        return pageMap[viewPager.currentItem]!!
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
                //设置底部tab @todo 添加底部tab icon
                topNavigation.visibility = View.GONE
                bottomNavigation.visibility = View.VISIBLE
                viewPager.addOnPageChangeListener(onPageChangeListener)
                for (i in 0 until Config.instance.tabCount) {
                    bottomNavigation.menu.add(Menu.NONE, i, i, Config.instance.tabTitles[i])
                }
                bottomNavigation.setOnNavigationItemSelectedListener(
                    onNavigationItemSelectedListener
                )
            }
        }
    }

}