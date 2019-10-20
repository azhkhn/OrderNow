package com.yue.ordernow.adapters

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yue.ordernow.R
import com.yue.ordernow.data.OrderItem
import com.yue.ordernow.databinding.ListItemOrderItemBinding
import com.yue.ordernow.databinding.ListItemOrderItemWithNoteBinding
import com.yue.ordernow.fragments.RestaurantMenuFragment


class OrderItemAdapter : ListAdapter<OrderItem, RecyclerView.ViewHolder>(OrderItemDiffCallback()) {

    companion object {
        const val TYPE_WITHOUT_NOTE = 0
        const val TYPE_WITH_NOTE = 1
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position).note) {
        "" -> TYPE_WITHOUT_NOTE
        else -> TYPE_WITH_NOTE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == TYPE_WITHOUT_NOTE) {
            OrderItemViewHolder(
                ListItemOrderItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            OrderItemWithNoteViewHolder(
                ListItemOrderItemWithNoteBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val menuItem = getItem(position)
        if (getItemViewType(position) == TYPE_WITHOUT_NOTE) {
            (holder as OrderItemViewHolder).bind(menuItem)
        } else {
            (holder as OrderItemWithNoteViewHolder).bind(menuItem)
        }

    }

    inner class OrderItemViewHolder(val binding: ListItemOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderItem) {
            binding.apply {
                orderItem = item
                executePendingBindings()
            }
        }
    }

    inner class OrderItemWithNoteViewHolder(val binding: ListItemOrderItemWithNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderItem) {
            binding.apply {
                orderItem = item
                executePendingBindings()
            }
        }
    }
}

private class OrderItemDiffCallback : DiffUtil.ItemCallback<OrderItem>() {

    override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean =
        oldItem == newItem
}

class OrderItemSwipeHelper(private val listener: OrderItemSwipeListener) :
    ItemTouchHelper.Callback() {

    interface OrderItemSwipeListener {
        fun onSwipe(itemPosition: Int)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int = makeMovementFlags(0, LEFT or RIGHT)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.onSwipe(viewHolder.adapterPosition)
    }

    override fun onChildDrawOver(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder?,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (viewHolder is OrderItemAdapter.OrderItemViewHolder) {
            getDefaultUIUtil().onDrawOver(
                c,
                recyclerView,
                viewHolder.binding.foreground,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        } else {
            getDefaultUIUtil().onDrawOver(
                c,
                recyclerView,
                (viewHolder as OrderItemAdapter.OrderItemWithNoteViewHolder).binding.foreground,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        // Get the current constraints
        val constraintSet = ConstraintSet()
        if (viewHolder is OrderItemAdapter.OrderItemViewHolder) {
            constraintSet.clone(viewHolder.binding.background)
        } else {
            constraintSet.clone((viewHolder as OrderItemAdapter.OrderItemWithNoteViewHolder).binding.background)
        }

        val margin =
            (listener as RestaurantMenuFragment).resources.getDimension(R.dimen.order_item_background_icon_margin)

        if (dX > 0) {
            // Clear the current end constraint
            constraintSet.clear(
                R.id.imageView,
                ConstraintSet.END
            )

            // Make new constraint
            constraintSet.connect(
                R.id.imageView,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                margin.toInt()
            )
        } else {
            // Clear the current end constraint
            constraintSet.clear(
                R.id.imageView,
                ConstraintSet.START
            )
            // Make new constraint
            constraintSet.connect(
                R.id.imageView,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                margin.toInt()
            )
        }

        if (viewHolder is OrderItemAdapter.OrderItemViewHolder) {
            // Apply the new constraints
            constraintSet.applyTo(viewHolder.binding.background)

            // Draw
            getDefaultUIUtil().onDraw(
                c,
                recyclerView,
                viewHolder.binding.foreground,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        } else {
            // Apply the new constraints
            constraintSet.applyTo((viewHolder as OrderItemAdapter.OrderItemWithNoteViewHolder).binding.background)

            // Draw
            getDefaultUIUtil().onDraw(
                c,
                recyclerView,
                viewHolder.binding.foreground,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is OrderItemAdapter.OrderItemViewHolder) {
            getDefaultUIUtil().clearView(viewHolder.binding.foreground)
        } else {
            getDefaultUIUtil().clearView((viewHolder as OrderItemAdapter.OrderItemWithNoteViewHolder).binding.foreground)
        }
    }
}