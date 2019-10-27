package com.yue.ordernow.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.yue.ordernow.adapters.OrderItemAdapter
import com.yue.ordernow.databinding.FragmentOrderDetailBinding

class OrderDetailFragment : Fragment() {

    val args: OrderDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        context ?: return binding.root

        val adapter = OrderItemAdapter(null)
        binding.orderItemList.adapter = adapter
        binding.orderItemList.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        adapter.submitList(args.order.orderItems)

        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                view!!.findNavController().navigateUp()
                super.onOptionsItemSelected(item)
            }
            else -> false
        }
}