package com.google.android.material.tabs

import com.ljx.layoutmanager.GridPagerLayoutManager
import com.ljx.view.IndicatorView

/**
 * User: ljx
 * Date: 2024/1/7
 * Time: 20:40
 */
class IndicatorObserver(
    private val indicatorView: IndicatorView,
    private val layoutManager: GridPagerLayoutManager,
) {

    private var attached = false

    private val onPageChangeListener = object : GridPagerLayoutManager.OnPageChangeListener() {

        override fun onPageCountChange(pageCount: Int) {
            indicatorView.setSliderCount(pageCount)
        }

        override fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
            indicatorView.scrollSlider(position, offset)
        }
    }

    fun attach() {
        check(!attached) { "IndicatorObserver is already attached" }
        attached = true
        layoutManager.registerOnPageChangeCallback(onPageChangeListener)
        indicatorView.setSliderCount(layoutManager.pageSize)
    }

    fun detach() {
        layoutManager.unregisterOnPageChangeCallback(onPageChangeListener)
        attached = false
    }

}