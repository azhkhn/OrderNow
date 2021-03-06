package com.yue.ordernow.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yue.ordernow.data.OrderRepository
import kotlinx.coroutines.launch
import java.util.*

class DashboardViewModel internal constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {
    val now = Calendar.getInstance()
    val orders = orderRepository.getAllOrders()
    val dailyOrders = orderRepository.getDailyOrders(now)
    val weeklyOrders = orderRepository.getWeeklyOrders(now)
    val monthlyOrders = orderRepository.getMonthlyOrders(now)
    val yearlyOrders = orderRepository.getYearlyOrders(now)

    fun deleteAllOrders() {
        viewModelScope.launch {
            orderRepository.deleteAllOrders()
        }
    }
}