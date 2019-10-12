package com.yue.ordernow.fragments


import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.yue.ordernow.R
import com.yue.ordernow.adapters.OrderItemAdapter
import com.yue.ordernow.data.OrderItem
import com.yue.ordernow.databinding.FragmentOrderListBinding
import com.yue.ordernow.utils.currencyFormat

private const val ORDERS = "orderItems"
private const val TOTAL_AMOUNT = "total_amount"


class OrderListFragment : Fragment() {

    private var orderItems: ArrayList<OrderItem>? = null
    private var subtotalAmount: Float? = null
    private var listener: OnOrderListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            orderItems = it.getParcelableArrayList(ORDERS)
            subtotalAmount = it.getFloat(TOTAL_AMOUNT)
        }
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentOrderListBinding.inflate(inflater, container, false)
        context ?: return binding.root

        // Add divider between each list item
        binding.orderList.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )

        // Set adapter
        val adapter = OrderItemAdapter()
        binding.orderList.adapter = adapter
        adapter.submitList(orderItems)

        // Set texts
        binding.subtotal.text = currencyFormat(subtotalAmount!!)
        binding.tax.text =
            currencyFormat(subtotalAmount!! * 0.13F) //TODO change hardcoded tax rate to something more flexible
        binding.totalAmount.text = currencyFormat(subtotalAmount!! * 1.13F)

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnOrderListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_order_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_clear -> {
            orderItems?.clear()

            // replace fragment
            listener?.onOrderListFragmentInteraction()
            true
        }
        R.id.action_send -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    interface OnOrderListFragmentInteractionListener {
        fun onOrderListFragmentInteraction()
    }

    companion object {

        @JvmStatic
        fun newInstance(orderItems: ArrayList<OrderItem>, totalAmount: Float) =
            OrderListFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ORDERS, orderItems)
                    putFloat(TOTAL_AMOUNT, totalAmount)
                }
            }
    }
}
