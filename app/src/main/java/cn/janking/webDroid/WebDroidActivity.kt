package cn.janking.webDroid

import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import cn.janking.webDroid.model.Config
import cn.janking.webDroid.widget.WebDroidView
import kotlinx.android.synthetic.main.activity_webdroid.*
import kotlinx.android.synthetic.main.fragment_webdroid.*
import kotlin.collections.HashMap

class WebDroidActivity : AppCompatActivity() {
    /**
     * 防止重定向的问题，此处记录真正的主页，即不会再重定向
     */
    private var configHomeUrl: String? = null

    private val mPageMap: MutableMap<Int, WebDroidView?> = HashMap()

    private val mPagerAdapter: PagerAdapter = object : PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getCount(): Int {
            return Config.instance.tabCount
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = getPageView(position)!!
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            container.addView(view, params)
            return view
        }

        private fun getPageView(position: Int): View? {
            var view = mPageMap[position]
            if (view == null) {
                view = WebDroidView.createView(
                    this@WebDroidActivity,
                    view_pager,
                    Config.instance.urls[position]
                )
                mPageMap[position] = view
            }
            return view.contentView
        }

        override fun destroyItem(
            container: ViewGroup,
            position: Int,
            `object`: Any
        ) {
            container.removeView(`object` as View)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return Config.instance.titles[position]
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webdroid)
        initTitle()
        initViews()
        initListeners()
    }

    private fun initTitle() {

    }

    private fun initViews() {
        view_pager.adapter = mPagerAdapter
        if (Config.instance.tabCount == 0) {
            tab_layout.visibility = View.GONE
        } else {
            tab_layout.visibility = View.VISIBLE
            tab_layout.setupWithViewPager(view_pager)
        }
    }

    private fun initListeners() {

    }

    /**
     * 监听返回键
     */
    override fun onBackPressed() {
        webView?.run {
            if (url != configHomeUrl && canGoBack()) {
                goBack()
                return
            }
        }
        if (Config.instance.preview) {
            super.onBackPressed()
        } else {
            moveTaskToBack(false);
        }
    }

}
