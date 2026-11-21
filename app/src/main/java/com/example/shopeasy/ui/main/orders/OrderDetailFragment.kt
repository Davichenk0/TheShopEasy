package com.example.shopeasy.ui.main.orders

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.shopeasy.R
import com.example.shopeasy.data.Order
import com.example.shopeasy.data.OrderRepository
import com.example.shopeasy.data.OrderStatus
import com.example.shopeasy.data.itemCount
import com.example.shopeasy.data.subtotal
import com.example.shopeasy.ui.common.asCurrency
import com.example.shopeasy.ui.common.asOrderDate
import com.example.shopeasy.ui.common.label
import com.example.shopeasy.ui.payment.PaymentActivity
import kotlinx.coroutines.launch

class OrderDetailFragment : Fragment() {

    private lateinit var tvOrderId: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvItems: TextView
    private lateinit var llOrderItems: LinearLayout
    private lateinit var tvTotal: TextView
    private lateinit var tvPaymentRef: TextView
    private lateinit var tvPaymentTransaction: TextView
    private lateinit var tvPaymentApproval: TextView
    private lateinit var tvPaymentFranchise: TextView
    private lateinit var paymentSection: View
    private lateinit var btnPay: Button
    private lateinit var btnCancel: Button
    private lateinit var btnDelete: Button
    private lateinit var progressBar: ProgressBar

    private var orderId: String = ""
    private var currentOrder: Order? = null

    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(requireContext(), R.string.payment_success_toast, Toast.LENGTH_LONG).show()
        }
        cargarPedido()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_order_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderId = arguments?.getString(ARG_ORDER_ID).orEmpty()

        tvOrderId = view.findViewById(R.id.tv_order_detail_id)
        tvDate = view.findViewById(R.id.tv_order_detail_date)
        tvStatus = view.findViewById(R.id.tv_order_detail_status)
        tvItems = view.findViewById(R.id.tv_order_detail_items_count)
        llOrderItems = view.findViewById(R.id.ll_order_items)
        tvTotal = view.findViewById(R.id.tv_order_detail_total)
        tvPaymentRef = view.findViewById(R.id.tv_payment_ref)
        tvPaymentTransaction = view.findViewById(R.id.tv_payment_transaction)
        tvPaymentApproval = view.findViewById(R.id.tv_payment_approval)
        tvPaymentFranchise = view.findViewById(R.id.tv_payment_franchise)
        paymentSection = view.findViewById(R.id.ll_payment_section)
        btnPay = view.findViewById(R.id.btn_order_pay)
        btnCancel = view.findViewById(R.id.btn_order_cancel)
        btnDelete = view.findViewById(R.id.btn_order_delete)
        progressBar = view.findViewById(R.id.pb_order_detail)

        btnPay.setOnClickListener { pagarPedido() }
        btnCancel.setOnClickListener { confirmarCancelacion() }
        btnDelete.setOnClickListener { confirmarEliminacion() }

        cargarPedido()
    }

    private fun cargarPedido() {
        viewLifecycleOwner.lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            try {
                val order = OrderRepository.getOrder(orderId)
                if (order == null) {
                    Toast.makeText(requireContext(), R.string.order_not_found, Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                    return@launch
                }
                currentOrder = order
                renderOrder(order)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.order_action_error, e.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun renderOrder(order: Order) {
        tvOrderId.text = getString(R.string.order_number, order.id.take(8).uppercase())
        tvDate.text = order.createdAt.asOrderDate()
        tvStatus.text = order.status.label()
        tvStatus.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                when (order.status) {
                    OrderStatus.PAID -> R.color.payment_success
                    OrderStatus.PENDING -> R.color.payment_pending
                    OrderStatus.FAILED -> R.color.payment_error
                    OrderStatus.CANCELLED -> R.color.gray
                }
            )
        )
        tvItems.text = getString(R.string.order_item_count, order.itemCount)
        tvTotal.text = order.total.asCurrency()

        llOrderItems.removeAllViews()
        order.items.forEach { item ->
            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_order_line, llOrderItems, false)
            row.findViewById<TextView>(R.id.tv_order_line_name).text = item.name
            row.findViewById<TextView>(R.id.tv_order_line_quantity).text =
                getString(R.string.order_quantity, item.quantity)
            row.findViewById<TextView>(R.id.tv_order_line_subtotal).text =
                item.subtotal.asCurrency()
            llOrderItems.addView(row)
        }

        val payment = order.payment
        paymentSection.visibility = if (payment == null) View.GONE else View.VISIBLE
        tvPaymentRef.text = payment?.refPayco ?: getString(R.string.order_payment_empty)
        tvPaymentTransaction.text = payment?.transactionId ?: getString(R.string.order_payment_empty)
        tvPaymentApproval.text = payment?.approvalCode ?: getString(R.string.order_payment_empty)
        tvPaymentFranchise.text = payment?.franchise ?: getString(R.string.order_payment_empty)

        btnPay.visibility = if (order.status == OrderStatus.PENDING ||
            order.status == OrderStatus.FAILED
        ) View.VISIBLE else View.GONE

        btnCancel.visibility =
            if (order.status == OrderStatus.PENDING) View.VISIBLE else View.GONE

        // Un pedido pagado se conserva como historial contable.
        btnDelete.visibility =
            if (order.status == OrderStatus.PAID) View.GONE else View.VISIBLE
    }

    private fun pagarPedido() {
        val order = currentOrder ?: return
        paymentLauncher.launch(PaymentActivity.createIntent(requireContext(), order.id))
    }

    private fun confirmarCancelacion() {
        val order = currentOrder ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.order_cancel_confirm_title)
            .setMessage(R.string.order_cancel_confirm)
            .setPositiveButton(R.string.order_cancel) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        OrderRepository.updateStatus(order.id, OrderStatus.CANCELLED)
                        Toast.makeText(requireContext(), R.string.order_cancelled_toast, Toast.LENGTH_SHORT).show()
                        cargarPedido()
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.order_action_error, e.message ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun confirmarEliminacion() {
        val order = currentOrder ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.order_delete_confirm_title)
            .setMessage(R.string.order_delete_confirm)
            .setPositiveButton(R.string.order_delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        OrderRepository.deleteOrder(order.id)
                        Toast.makeText(requireContext(), R.string.order_deleted_toast, Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.order_action_error, e.message ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    companion object {
        private const val ARG_ORDER_ID = "order_id"

        fun newInstance(orderId: String): OrderDetailFragment =
            OrderDetailFragment().apply {
                arguments = Bundle().apply { putString(ARG_ORDER_ID, orderId) }
            }
    }
}
