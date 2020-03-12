package cn.janking.webDroid.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.janking.webDroid.R
import cn.janking.webDroid.util.AppUtils
import cn.janking.webDroid.util.Utils
import java.util.*

/**
 * @author Janking
 */
class TabListRVAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), OnMoveAndSwipedListener {
    val tabTitleItems: MutableList<CharSequence> = ArrayList()
    val tabUrlItems: MutableList<CharSequence> = ArrayList()
    private val typeNormal = 1

    fun addTabItem() {
        addTabItem("", "")
    }

    fun addTabItem(title: String, url: String) {
        if (tabTitleItems.size > 4) {
            Toast.makeText(Utils.getApp(), "最多只能添加5个tab", Toast.LENGTH_SHORT).show()
            return
        }
        tabTitleItems.add(tabTitleItems.size, title)
        tabUrlItems.add(tabUrlItems.size, url)
        notifyItemInserted(tabTitleItems.size - 1)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycler_view, parent, false)
        return TabItemViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (holder is TabItemViewHolder) {
            holder.tabItemTitle.setText(tabTitleItems[position])
            holder.tabItemTitle.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let {
                        if (tabTitleItems.size > position) {
                            tabTitleItems[position] = it
                        }
                    }
                }
            })

            holder.tabItemUrl.setText(tabUrlItems[position])
            holder.tabItemUrl.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s?.let {
                        if (tabUrlItems.size > position) {
                            tabUrlItems[position] = it
                        }
                    }
                }
            })
        }
    }

    override fun getItemViewType(position: Int): Int {
        return typeNormal
    }

    override fun getItemCount(): Int {
        return tabTitleItems.size
    }

    /**
     * 移动事件
     */
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(tabTitleItems, fromPosition, toPosition)
        Collections.swap(tabUrlItems, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    /**
     * 删除事件
     */
    override fun onItemDismiss(position: Int) {
        tabTitleItems.removeAt(position)
        tabUrlItems.removeAt(position)
        notifyItemRemoved(position)
    }
}

class TabItemViewHolder constructor(view: View) :
    RecyclerView.ViewHolder(view) {
    val tabItemTitle: EditText = view.findViewById(R.id.itemTabTitle)
    val tabItemUrl: EditText = view.findViewById(R.id.itemTabUrl)
}