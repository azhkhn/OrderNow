package com.yue.ordernow.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yue.ordernow.R
import com.yue.ordernow.activities.ReportDetailActivity
import com.yue.ordernow.adapters.OrderAdapter
import com.yue.ordernow.data.Report
import com.yue.ordernow.databinding.FragmentReportDetailBinding
import com.yue.ordernow.utilities.InjectorUtils
import com.yue.ordernow.utilities.PercentFormatter
import com.yue.ordernow.utilities.getThemeColor
import com.yue.ordernow.viewModels.ReportDetailFragmentViewModel
import java.util.*
import kotlin.collections.ArrayList

class ReportDetailFragment : Fragment() {

    private val hostActivity by lazy {
        activity as ReportDetailActivity
    }

    private val viewModel: ReportDetailFragmentViewModel by viewModels {
        InjectorUtils.provideReportDetailFragmentViewModelFactory(
            requireContext(),
            hostActivity.viewModel.reportType,
            Calendar.getInstance().apply {
                timeInMillis = hostActivity.viewModel.timeStamp
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentReportDetailBinding.inflate(inflater, container, false).apply {
            this.reportSummary.layoutParams.height = resources.displayMetrics.widthPixels / 2
            setupOrderList(this.orderList)
            setHasOptionsMenu(true)
            setupToolBar(this.toolbar)
            setupPieChart(this.pieChart)
            addPieChartData(this.pieChart)
            setupCustomLegend(
                this.diningInQty,
                this.takeoutQty,
                this.totalQty
            )
        }
        context ?: return binding.root

        return binding.root
    }

    private fun setupToolBar(toolbar: Toolbar) {
        toolbar.title = when (hostActivity.viewModel.reportType) {
            Report.Type.TODAY -> resources.getString(R.string.today)
            Report.Type.THIS_WEEK -> resources.getString(R.string.this_week)
            Report.Type.THIS_MONTH -> resources.getString(R.string.this_month)
        }

        toolbar.setNavigationOnClickListener {
            activity?.finish()
            activity?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun setupOrderList(orderList: RecyclerView) {
        val adapter = OrderAdapter(requireContext())
        orderList.adapter = adapter
        orderList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            adapter.submitList(orders)
        }
    }

    private fun setupPieChart(pieChart: PieChart) {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        pieChart.setDrawEntryLabels(false)
        pieChart.setHoleColor(hostActivity.getThemeColor(android.R.attr.colorBackground))
        pieChart.animateY(1400, Easing.EaseInOutQuad)
        pieChart.legend.isEnabled = false
    }

    private fun addPieChartData(pieChart: PieChart) {
        val dataEntries = ArrayList<PieEntry>(2).apply {
            add(
                PieEntry(
                    hostActivity.viewModel.takeoutCount.toFloat(),
                    resources.getString(R.string.take_out)
                )
            )
            add(
                PieEntry(
                    hostActivity.viewModel.diningInCount.toFloat(),
                    resources.getString(R.string.dining_in)
                )
            )
        }

        val dataSet = PieDataSet(dataEntries, "").apply {
            setDrawIcons(false)
            colors = listOf(
                ContextCompat.getColor(hostActivity, R.color.color_takeout),
                ContextCompat.getColor(hostActivity, R.color.color_dining_in)
            )
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            sliceSpace = 3f
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(pieChart))
        }

        pieChart.data = data
    }

    private fun setupCustomLegend(
        diningInQty: TextView,
        takeoutQty: TextView,
        totalQty: TextView
    ) {
        diningInQty.text = hostActivity.viewModel.diningInCount.toString()
        takeoutQty.text = hostActivity.viewModel.takeoutCount.toString()
        totalQty.text =
            (hostActivity.viewModel.diningInCount + hostActivity.viewModel.takeoutCount).toString()
    }

}