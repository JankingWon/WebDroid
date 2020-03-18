package cn.janking.webDroid.layout

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.janking.webDroid.R
import cn.janking.webDroid.adapter.ItemTouchHelperCallback
import cn.janking.webDroid.adapter.TabListRVAdapter
import cn.janking.webDroid.model.Config

/**
 * @author Janking
 */
class EditTabLayout(activity: Activity) : EditLayout() {
    /**
     * 视图
     */
    override val contentView = LayoutInflater.from(activity)
        .inflate(R.layout.layout_edit_tab, null) as LinearLayout

    /**
     * 配置tab列表的适配器
     */
    var tabListAdapter: TabListRVAdapter = TabListRVAdapter()

    /**
     * 添加Tab
     */
    val addTab = contentView.findViewById<ImageButton>(R.id.addTab).apply {
        setOnClickListener {
            tabListAdapter.addTabItem()
        }
    }

    /**
     * 编辑tab列表
     */
    val tabList = contentView.findViewById<RecyclerView>(R.id.tabList).apply {
        //tab设置
        layoutManager = LinearLayoutManager(activity)
        adapter = tabListAdapter
    }

    /**
     * tab样式
     */
    val tabStyle = contentView.findViewById<RadioGroup>(R.id.itemTabStyle).apply {
        setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.itemTabStyleTop) {
                for (i in 0 until tabList.childCount) {
                    tabList[i].findViewById<ImageView>(R.id.itemTabIcon).visibility = View.GONE
                }
                //即时更新config
                Config.instance.tabStyle = 0
            } else if (checkedId == R.id.itemTabStyleBottom) {
                for (i in 0 until tabList.childCount) {
                    tabList[i].findViewById<ImageButton>(R.id.itemTabIcon).visibility = View.VISIBLE
                }
                //即时更新config
                Config.instance.tabStyle = 1
            }
        }
    }

    init {
        //tab滑动删除和移动的手势操作
        val callback: ItemTouchHelper.Callback = ItemTouchHelperCallback(tabListAdapter)
        val mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(tabList)
        //load
        loadLastConfig()
    }

    override fun loadLastConfig() {
        for (i in 0 until Config.instance.tabCount) {
            tabListAdapter.addTabItem(
                Config.instance.tabTitles[i],
                Config.instance.tabUrls[i],
                Config.instance.tabIcons[i]
            )
        }
    }

    override fun generateConfig() {
        Config.instance.let {
            it.tabTitles = tabListAdapter.tabTitleItems.map { item ->
                item.toString()
            }
            it.tabUrls = tabListAdapter.tabUrlItems.map { item ->
                item.toString()
            }
            it.tabIcons = tabListAdapter.tabIconItems.map { item ->
                item.toString()
            }
            it.tabCount = it.tabTitles.size.coerceAtMost(it.tabUrls.size)
        }
    }
}