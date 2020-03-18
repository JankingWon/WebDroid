package cn.janking.webDroid.layout

import android.app.Activity
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
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
     * 编辑tab列表
     */
    val tabList = contentView.findViewById<RecyclerView>(R.id.tabList)

    /**
     * 添加Tab
     */
    val addTab = contentView.findViewById<ImageButton>(R.id.addTab)

    /**
     * 配置tab列表的适配器
     */
    var tabListAdapter: TabListRVAdapter = TabListRVAdapter(activity)

    init {
        //tab设置
        tabList.layoutManager = LinearLayoutManager(activity)
        tabList.adapter = tabListAdapter
        //添加Tab
        addTab.setOnClickListener {
            tabListAdapter.addTabItem()
        }
        //tab滑动删除和移动的手势操作
        val callback: ItemTouchHelper.Callback = ItemTouchHelperCallback(tabListAdapter)
        val mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(tabList)
        //load
        loadLastConfig()
    }

    override fun loadLastConfig() {
        for (i in 0 until Config.instance.tabCount) {
            tabListAdapter.addTabItem(Config.instance.tabTitles[i], Config.instance.tabUrls[i])
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
            it.tabCount = it.tabTitles.size.coerceAtMost(it.tabUrls.size)
        }
    }
}