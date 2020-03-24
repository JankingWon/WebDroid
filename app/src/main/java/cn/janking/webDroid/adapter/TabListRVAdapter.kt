package cn.janking.webDroid.adapter

import android.graphics.BitmapFactory
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import cn.janking.webDroid.R
import cn.janking.webDroid.util.*
import java.io.File
import java.util.*

/**
 * @author Janking
 */
class TabListRVAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), OnMoveAndSwipedListener {
    companion object {
        const val SELECT_FILE_REQUEST_CODE_MIN = 100
        const val SELECT_FILE_REQUEST_CODE_MAX = 104
    }

    /**
     * 标题列表
     */
    val tabTitleItems: MutableList<CharSequence> = ArrayList()

    /**
     * url列表
     */
    val tabUrlItems: MutableList<CharSequence> = ArrayList()

    /**
     * icon列表，表示文件位置
     */
    val tabIconItems: MutableList<String> = ArrayList()

    /**
     * 普通类型的item
     */
    private val typeNormal = 1

    fun addTabItem() {
        addTabItem("", "", "")
    }

    fun addTabItem(title: String, url: String, icon: String) {
        val tabCount = tabTitleItems.size
        if (tabCount > 4) {
            Toast.makeText(Utils.getApp(), "最多只能添加5个tab", Toast.LENGTH_SHORT).show()
            return
        }
        tabTitleItems.add(tabCount, title)
        tabUrlItems.add(tabCount, url)
        tabIconItems.add(tabCount, icon)
        notifyItemInserted(tabCount)
    }

    fun clearAllTab() {
        val count = tabTitleItems.size
        tabTitleItems.clear()
        tabUrlItems.clear()
        tabIconItems.clear()
        notifyItemRangeRemoved(0, count)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tab_list, parent, false)
        return TabItemViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (holder is TabItemViewHolder) {
            //tab标题
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
            //tab URL
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
            //tab ICON
            //设置默认icon
            holder.tabItemIcon.setImageResource(R.drawable.ic_tab_0)
            //加载config的icon
            tabIconItems[position].let {
                File(it).run {
                    //试试文件
                    if (FileUtils.isFileExists(this)) {
                        holder.tabItemIcon.setImageURI(UriUtils.file2Uri(this))
                    } else {
                        //试试assets
                        try {
                            holder.tabItemIcon.setImageBitmap(
                                BitmapFactory.decodeStream(
                                    Utils.getApp().assets.open("template/$it")
                                )
                            )
                        } catch (ignore: Exception) {
                            //ignore
                        }
                    }
                }
            }
            //点击选取icon
            holder.tabItemIcon.setOnClickListener {
                OpenUtils.toSelectFile("image/*", SELECT_FILE_REQUEST_CODE_MIN + position)
            }
            //长按预览icon
            holder.tabItemIcon.setOnLongClickListener {
                return@setOnLongClickListener OpenUtils.showFullImageDialogWithFile(tabIconItems[position])
            }
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
        Collections.swap(tabIconItems, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    /**
     * 删除事件
     */
    override fun onItemDismiss(position: Int) {
        tabTitleItems.removeAt(position)
        tabUrlItems.removeAt(position)
        tabIconItems.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * 选择图标返回
     */
    fun onSelectImageResult(position: Int, uri: Uri) {
        ThreadUtils.executeByIo(object : ThreadUtils.SimpleTask<String>() {
            override fun doInBackground(): String {
                return FileUtils.copyUriToTempFile(uri).toString()
            }

            override fun onSuccess(result: String) {
                tabIconItems[position] = result
                Utils.runOnUiThread {
                    notifyItemChanged(position)
                }
            }

        })
    }
}

/**
 * ViewHolder
 */
class TabItemViewHolder constructor(view: View) :
    RecyclerView.ViewHolder(view) {
    val tabItemIcon: ImageView = view.findViewById(R.id.itemTabIcon)
    val tabItemTitle: EditText = view.findViewById(R.id.itemTabTitle)
    val tabItemUrl: EditText = view.findViewById(R.id.itemTabUrl)
}