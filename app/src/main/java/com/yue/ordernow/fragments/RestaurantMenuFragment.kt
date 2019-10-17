package com.yue.ordernow.fragments


import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import com.yue.ordernow.R
import com.yue.ordernow.activities.MainActivity
import com.yue.ordernow.adapters.*
import com.yue.ordernow.data.MenuItem
import com.yue.ordernow.data.Order
import com.yue.ordernow.data.OrderItem
import com.yue.ordernow.databinding.FragmentRestaurantMenuBinding
import com.yue.ordernow.utils.currencyFormat
import com.yue.ordernow.viewModels.MainViewModel


class RestaurantMenuFragment : Fragment(), AddNoteDialogFragment.AddNoteDialogListener,
    MenuItemAdapter.MenuItemListener {

    private val adapter = OrderItemAdapter()
    private lateinit var binding: FragmentRestaurantMenuBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var activityViewModel: MainViewModel
    private var isOptionMenuViable = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        if (activity is MainActivity) {
            activityViewModel = (activity as MainActivity).viewModel
        } else {
            throw IllegalAccessException("Illegal parent activity")
        }

        binding = FragmentRestaurantMenuBinding.inflate(inflater, container, false)
        binding.menuPage.adapter = MenuPageViewAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.menuPage) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()

        // Add divider between each list item
        binding.bottomSheet.orderList.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )

        // Set adapter
        binding.bottomSheet.orderList.adapter = adapter
        adapter.submitList(activityViewModel.orderItems)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)
        updateBottomSheetBehavior()
        binding.bottomSheet.onHeaderClickListener = OnBottomSheetHeaderClickListener()
        bottomSheetBehavior.addBottomSheetCallback(BottomSheetCallback())

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_confirm).isVisible = isOptionMenuViable
        menu.findItem(R.id.action_clear).isVisible = isOptionMenuViable
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean = when (item.itemId) {
        R.id.action_confirm -> {
            // Sort all order items
            activityViewModel.orderItems.sortWith(Comparator { t, t2 ->
                t.item.name.compareTo(t2.item.name)
            })

            // Create order object and save it to data base
            activityViewModel.saveToDatabase(
                Order.newInstance(
                    activityViewModel.orderItems,
                    activityViewModel.subtotal,
                    activityViewModel.totalQuantity
                )
            )
            clearOrders()
            true
        }
        R.id.action_clear -> {
            clearOrders()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /*
    * AddNoteDialogFragment.AddNoteDialogListener method
    */

    override fun onDialogPositiveClick(dialog: DialogFragment, orderItem: OrderItem) {
        addOrder(orderItem)
    }

    /*
     * MenuItemAdapter.MenuItemListener methods
     */

    override fun onOrderButtonClick(menuItem: MenuItem?) {
        menuItem?.let {
            addOrder(OrderItem(it, 1, ""))
        }
    }

    override fun onCustomizeButtonClick(menuItem: MenuItem?) {
        menuItem?.let {
            AddNoteDialogFragment(this, it.copy()) // MUST pass a copy here
                .show(childFragmentManager, "")
        }
    }

    /*
     * Private methods
     */

    private fun addOrder(orderItem: OrderItem) {
        activityViewModel.totalQuantity += orderItem.quantity
        activityViewModel.subtotal += orderItem.quantity * orderItem.item.price

        for (it in (activity as MainActivity).viewModel.orderItems) {
            if (it.item == orderItem.item && it.note == orderItem.note) {

                // combine the two orderItems
                it.quantity += orderItem.quantity

                // Update view
                updateBottomSheetBehavior()
                return
            }
        }

        (activity as MainActivity).viewModel.orderItems.add(orderItem)
        updateBottomSheetBehavior()
    }

    private fun clearOrders() {
        activityViewModel.totalQuantity = 0
        activityViewModel.subtotal = 0f
        (activity as MainActivity).viewModel.orderItems.clear()
        updateBottomSheetBehavior()
    }

    private fun updateBottomSheetBehavior() {
        if ((activity as MainActivity).viewModel.orderItems.isEmpty()) {
            // Hide bottom sheet if there is no order item
            bottomSheetBehavior.isHideable = true
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            // Update view
            // Note the notifyDateSetChange() needs to be called after bottom sheet behaviors are set
            // Otherwise the bottom sheet will expend and collapse quickly, it doesn't look nice
            bottomSheetBehavior.isHideable = false
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            adapter.notifyDataSetChanged()
        }
        updateBottomSheetText()
    }

    private fun updateBottomSheetText() {
        binding.bottomSheet.quantity.text = activityViewModel.totalQuantity.toString()
        binding.bottomSheet.totalAmount.text =
            currencyFormat((activityViewModel.subtotal * 1.13).toFloat())
        binding.bottomSheet.tax.text = currencyFormat((activityViewModel.subtotal * 0.13).toFloat())
        binding.bottomSheet.subtotal.text = currencyFormat(activityViewModel.subtotal)
    }

    private fun getTabTitle(position: Int): String? = when (position) {
        APPETIZER_PAGE_INDEX -> getString(R.string.appetizer_title)
        BREAKFAST_PAGE_INDEX -> getString(R.string.breakfast_title)
        MAIN_PAGE_INDEX -> getString(R.string.main_title)
        DRINK_PAGE_INDEX -> getString(R.string.drink_title)
        else -> null
    }

    private inner class OnBottomSheetHeaderClickListener : View.OnClickListener {
        override fun onClick(p0: View?) {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private inner class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                (activity as MainActivity).supportActionBar?.title = getString(R.string.title_menu)
                isOptionMenuViable = false
                activity?.invalidateOptionsMenu()
            } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                (activity as MainActivity).supportActionBar?.title =
                    getString(R.string.title_current_order)
                isOptionMenuViable = true
                activity?.invalidateOptionsMenu()
            }
        }

    }
}
