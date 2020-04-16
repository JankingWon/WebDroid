package cn.janking.webDroid.adapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView拖动和滑动删除的手势操作
 */
class ItemTouchHelperCallback(private val moveAndSwipedListener: OnMoveAndSwipedListener) :
    ItemTouchHelper.Callback() {
    private val typeNormal = 1
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return if (recyclerView.layoutManager is GridLayoutManager) {
            // 支持上下左右拖动，不支持删除
            val dragFlags =
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            makeMovementFlags(
                dragFlags,
                swipeFlags
            )
        } else {
            //支持上下拖动，支持左右删除
            if (viewHolder.itemViewType == typeNormal) {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
                makeMovementFlags(
                    dragFlags,
                    swipeFlags
                )
            } else {
                0
            }
        }
    }

    /**
     * 滑动移动
     */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        //只支持同样类型的ITEM
        if (viewHolder.itemViewType != target.itemViewType) {
            return false
        }
        if (viewHolder is TabItemViewHolder && target is TabItemViewHolder) {
            viewHolder.tabItemTitle.clearFocus()
            viewHolder.tabItemUrl.clearFocus()
            target.tabItemTitle.clearFocus()
            target.tabItemUrl.clearFocus()
        }
        //回调
        moveAndSwipedListener.onItemMove(
            viewHolder.adapterPosition,
            target.adapterPosition
        )
        return true
    }

    /**
     * 滑动删除
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        //回调
        moveAndSwipedListener.onItemDismiss(viewHolder.adapterPosition)
    }

}