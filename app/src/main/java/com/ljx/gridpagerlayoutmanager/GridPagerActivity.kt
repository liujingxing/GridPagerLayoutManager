package com.ljx.gridpagerlayoutmanager

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.ljx.gridpagerlayoutmanager.base.BaseAdapter
import com.ljx.gridpagerlayoutmanager.databinding.GridPagerActivityBinding
import com.ljx.gridpagerlayoutmanager.databinding.GridPagerAdapterBinding
import com.ljx.layoutmanager.GridPagerLayoutManager

/**
 * User: ljx
 * Date: 2023/12/30
 * Time: 18:43
 */
class GridPagerActivity : FragmentActivity(), View.OnClickListener {
    private lateinit var binding: GridPagerActivityBinding

    private val pageLayoutManager: GridPagerLayoutManager
        get() = binding.recyclerView.layoutManager as GridPagerLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        binding = DataBindingUtil.setContentView(this, R.layout.grid_pager_activity)
        binding.init()
    }

    private fun GridPagerActivityBinding.init() {
        mutableListOf(
            btFirstPage, btNextPage, btPreviousPage, btLastPage,
            ivRowsMinus, ivRowsPlus, ivColumnsMinus, ivColumnsPlus,
            ivPaddingMinus, ivPaddingPlus
        ).forEach { it.setOnClickListener(this@GridPagerActivity) }

        val queues = LinkedHashMap<String, List<Int>>()
        val dataList = mutableListOf<Int>()
        for (i in 0 until 4) {
            dataList.add(i)
        }
        queues["队列1"] = dataList
        queues["队列2"] = dataList
        queues["队列3"] = mutableListOf(1)

        val totalList = mutableListOf<Int>()
        queues.values.forEach { totalList.addAll(it) }
        recyclerView.adapter = NumberAdapter(totalList)
        pageLayoutManager.setQueueInfo(queues.values, intArrayOf(8))
        pageLayoutManager.attach(indicatorView)
        pageLayoutManager.attach(tabLayout) { tab, position ->
            tab.setText(queues.keys.toList()[position])
        }

        rgOrientation.setOnCheckedChangeListener { _, checkedId ->
            changeOrientation(checkedId)
        }
        rgLayoutDirection.setOnCheckedChangeListener { _, checkedId ->
            changeLayoutDirection(checkedId)
        }
        rgReverseLayout.setOnCheckedChangeListener { _, checkedId ->
            changeReverseLayout(checkedId)
        }

        etRows.addTextChangedListener {
            val rowCount = it.toString().toIntOrNull() ?: 2
            pageLayoutManager.rowCount = rowCount
        }
        etColumns.addTextChangedListener {
            val columnCount = it.toString().toIntOrNull() ?: 4
            pageLayoutManager.columnCount = columnCount
        }
        etPadding.addTextChangedListener {
            val dp = (it.toString().toIntOrNull() ?: 0).dp.toInt()
            recyclerView.setPadding(dp, dp, dp, dp)
        }
        cbPadding.setOnCheckedChangeListener { buttonView, isChecked ->
            recyclerView.clipToPadding = isChecked
        }
    }

    private fun changeOrientation(checkedId: Int) {
        val orientation =
            if (checkedId == R.id.rb_horizontal) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL
        pageLayoutManager.orientation = orientation
        binding.indicatorView.setOrientation(orientation)

        binding.indicatorView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            leftToLeft =
                if (orientation == RecyclerView.VERTICAL) ConstraintLayout.LayoutParams.UNSET
                else ConstraintLayout.LayoutParams.PARENT_ID
        }
    }

    private fun changeLayoutDirection(checkedId: Int) {
        val layoutDirection =
            if (checkedId == R.id.rb_ltr) View.LAYOUT_DIRECTION_LTR else View.LAYOUT_DIRECTION_RTL
        binding.recyclerView.layoutDirection = layoutDirection
        binding.indicatorView.layoutDirection = layoutDirection
    }

    private fun changeReverseLayout(checkedId: Int) {
        val reverseLayout = checkedId == R.id.rb_yes
        pageLayoutManager.reverseLayout = reverseLayout
        binding.indicatorView.setReverseLayout(reverseLayout)
    }

    class NumberAdapter(dataList: List<Int>) :
        BaseAdapter<Int, GridPagerAdapterBinding>(dataList, R.layout.grid_pager_adapter) {

        override fun GridPagerAdapterBinding.onBindViewHolder(item: Int, position: Int) {
            tvNumber.text = "$item"
            val color = when (position % 8) {
                0 -> "#E37E7E"
                1 -> "#E49542"
                2 -> "#FF03DAC5"
                3 -> "#FF018786"
                4 -> "#FF018786"
                5 -> "#FF03DAC5"
                6 -> "#E49542"
                else -> "#E37E7E"
            }
            tvNumber.setBackgroundColor(Color.parseColor(color))
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bt_first_page -> pageLayoutManager.scrollFirstPage(true)
            R.id.bt_next_page -> pageLayoutManager.scrollNextPage(true)
            R.id.bt_previous_page -> pageLayoutManager.scrollPreviousPage(true)
            R.id.bt_last_page -> pageLayoutManager.scrollLastPage(true)
            R.id.iv_rows_minus -> {
                val dp = binding.etRows.text.toString().toInt() - 1
                binding.etRows.setText(dp.coerceAtLeast(1).toString())
            }

            R.id.iv_rows_plus -> {
                val dp = binding.etRows.text.toString().toInt() + 1
                binding.etRows.setText(dp.coerceAtLeast(1).toString())
            }

            R.id.iv_columns_minus -> {
                val dp = binding.etColumns.text.toString().toInt() - 1
                binding.etColumns.setText(dp.coerceAtLeast(1).toString())
            }

            R.id.iv_columns_plus -> {
                val dp = binding.etColumns.text.toString().toInt() + 1
                binding.etColumns.setText(dp.coerceAtLeast(1).toString())
            }

            R.id.iv_padding_minus -> {
                val dp = binding.etPadding.text.toString().toInt() - 1
                binding.etPadding.setText(dp.coerceAtLeast(0).toString())
            }

            R.id.iv_padding_plus -> {
                val dp = binding.etPadding.text.toString().toInt() + 1
                binding.etPadding.setText(dp.coerceAtLeast(1).toString())
            }
        }
    }

    private val Int.dp: Float
        get() {
            val scale = Resources.getSystem().displayMetrics.density
            return this * scale
        }
}


