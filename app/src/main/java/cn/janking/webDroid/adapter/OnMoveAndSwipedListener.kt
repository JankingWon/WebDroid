package cn.janking.webDroid.adapter

/**
 * Created by zhang on 2016.08.21.
 */
interface OnMoveAndSwipedListener {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
    fun onItemDismiss(position: Int)
}