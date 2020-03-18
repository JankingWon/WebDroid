package cn.janking.webDroid.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

/**
 * @author Janking
 */
abstract class BasicPagerAdapter : PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    /**
     * 需要重写
     */
/*    override fun getCount(): Int {
        return 0
    }*/

    /**
     * 需要重写
     */
/*    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return null
    }*/

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        container.removeView(`object` as View)
    }

    /**
     * 需要重写
     */
/*    override fun getPageTitle(position: Int): CharSequence? {

    }*/
}