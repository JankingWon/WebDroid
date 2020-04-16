package cn.janking.webDroid.adapter

/**
 * 拖动和滑动删除的监听器
 */
interface OnMoveAndSwipedListener {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
    fun onItemDismiss(position: Int)
}