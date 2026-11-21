package com.example.shopeasy.ui.payment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.shopeasy.R
import com.example.shopeasy.data.CartRepository
import com.example.shopeasy.data.Order
import com.example.shopeasy.data.OrderRepository
import com.example.shopeasy.data.OrderStatus
import com.example.shopeasy.payments.EpaycoConfig
import com.example.shopeasy.payments.EpaycoPaymentService
import com.example.shopeasy.payments.EpaycoTransaction
import com.example.shopeasy.ui.common.asCurrency
import kotlinx.coroutines.launch
import kotlin.math.abs

class PaymentActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var resultPanel: View
    private lateinit var resultTitle: TextView
    private lateinit var resultMessage: TextView
    private lateinit var resultAmount: TextView
    private lateinit var closeButton: Button

    private var orderId: String = ""
    private var currentOrder: Order? = null
    private var resultHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        orderId = intent.getStringExtra(EXTRA_ORDER_ID).orEmpty()
        if (orderId.isBlank()) {
            finish()
            return
        }

        bindViews()
        configureWebView()
        setupBackNavigation()
        loadCheckout()
    }

    private fun bindViews() {
        webView = findViewById(R.id.webview_payment)
        progressBar = findViewById(R.id.pb_payment)
        statusText = findViewById(R.id.tv_payment_status)
        resultPanel = findViewById(R.id.payment_result_panel)
        resultTitle = findViewById(R.id.tv_payment_result_title)
        resultMessage = findViewById(R.id.tv_payment_result_message)
        resultAmount = findViewById(R.id.tv_payment_result_amount)
        closeButton = findViewById(R.id.btn_payment_close)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView.setBackgroundColor(getColor(R.color.white))

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean = handleResponseUrl(request.url.toString())

            @Deprecated("Compatibilidad con WebView antiguos")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean =
                handleResponseUrl(url)

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url != null) handleResponseUrl(url)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame && !resultHandled) {
                    showError(getString(R.string.payment_webview_error))
                }
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack() && resultPanel.visibility != View.VISIBLE) {
                    webView.goBack()
                } else {
                    finishWithStatus(RESULT_CANCELED)
                }
            }
        })
    }

    private fun loadCheckout() {
        showLoading(getString(R.string.payment_loading))

        lifecycleScope.launch {
            try {
                val order = OrderRepository.getOrder(orderId)
                if (order == null) {
                    showError(getString(R.string.payment_order_not_found))
                    return@launch
                }
                currentOrder = order

                if (order.status == OrderStatus.PAID) {
                    showResult(OrderStatus.PAID, null)
                    return@launch
                }
                if (!EpaycoConfig.isConfigured) {
                    showError(getString(R.string.payment_not_configured))
                    return@launch
                }

                val body = EpaycoPaymentService.buildCheckoutBody(order)
                progressBar.visibility = View.GONE
                statusText.visibility = View.GONE
                webView.visibility = View.VISIBLE
                webView.postUrl(EpaycoConfig.CHECKOUT_URL, body.toByteArray(Charsets.UTF_8))
            } catch (e: Exception) {
                showError(e.message ?: getString(R.string.payment_error))
            }
        }
    }

    private fun handleResponseUrl(url: String): Boolean {
        if (!url.startsWith(EpaycoConfig.RESPONSE_URL, ignoreCase = true)) return false

        val uri = Uri.parse(url)
        val refPayco = uri.getQueryParameter("ref_payco")
            ?: uri.getQueryParameter("x_ref_payco")

        confirmPayment(refPayco)
        return true
    }

    private fun confirmPayment(refPayco: String?) {
        if (resultHandled) return
        resultHandled = true
        webView.visibility = View.GONE

        if (refPayco.isNullOrBlank()) {
            showResult(OrderStatus.CANCELLED, null)
            return
        }

        showLoading(getString(R.string.payment_confirming))

        lifecycleScope.launch {
            try {
                val order = currentOrder ?: OrderRepository.getOrder(orderId)
                if (order == null) {
                    showError(getString(R.string.payment_order_not_found))
                    return@launch
                }

                val transaction = EpaycoPaymentService.validateTransaction(refPayco)
                val status = resolveStatus(order, transaction)

                OrderRepository.updateStatus(order.id, status, transaction.toPaymentDetails())
                if (status == OrderStatus.PAID) {
                    CartRepository.clear()
                }

                showResult(status, transaction)
            } catch (e: Exception) {
                showError(
                    getString(R.string.payment_confirmation_error, e.message ?: "")
                )
            }
        }
    }

    private fun resolveStatus(order: Order, transaction: EpaycoTransaction): OrderStatus {
        val invoiceMatches = transaction.invoice.isBlank() || transaction.invoice == order.id
        val amountMatches = abs(transaction.amount - order.total) < 1.0
        val signatureMatches = transaction.signatureValid != false
        val currencyMatches = transaction.currency.isBlank() ||
            transaction.currency.equals(EpaycoConfig.CURRENCY, ignoreCase = true)

        return when {
            !invoiceMatches || !amountMatches || !signatureMatches || !currencyMatches ->
                OrderStatus.FAILED
            transaction.isApproved -> OrderStatus.PAID
            transaction.isPending -> OrderStatus.PENDING
            else -> OrderStatus.FAILED
        }
    }

    private fun showResult(status: OrderStatus, transaction: EpaycoTransaction?) {
        showPanel()

        val approved = status == OrderStatus.PAID
        resultTitle.text = when (status) {
            OrderStatus.PAID -> getString(R.string.payment_approved)
            OrderStatus.PENDING -> getString(R.string.payment_pending)
            OrderStatus.CANCELLED -> getString(R.string.payment_cancelled)
            OrderStatus.FAILED -> getString(R.string.payment_rejected)
        }
        resultTitle.setTextColor(
            getColor(if (approved) R.color.payment_success else R.color.payment_error)
        )
        resultMessage.text = transaction?.reason?.takeIf { it.isNotBlank() }
            ?: getString(
                if (approved) R.string.payment_approved_message
                else R.string.payment_result_message
            )
        resultAmount.text = currentOrder?.total?.asCurrency().orEmpty()

        closeButton.setOnClickListener {
            setResult(if (approved) RESULT_OK else RESULT_CANCELED)
            finish()
        }
    }

    private fun showError(message: String) {
        showPanel()
        resultTitle.text = getString(R.string.payment_error_title)
        resultTitle.setTextColor(getColor(R.color.payment_error))
        resultMessage.text = message
        resultAmount.text = currentOrder?.total?.asCurrency().orEmpty()
        closeButton.setOnClickListener { finishWithStatus(RESULT_CANCELED) }
    }

    private fun showLoading(message: String) {
        resultPanel.visibility = View.GONE
        webView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        statusText.visibility = View.VISIBLE
        statusText.text = message
    }

    private fun showPanel() {
        webView.visibility = View.GONE
        progressBar.visibility = View.GONE
        statusText.visibility = View.GONE
        resultPanel.visibility = View.VISIBLE
    }

    private fun finishWithStatus(resultCode: Int) {
        setResult(resultCode)
        finish()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.destroy()
        }
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_ORDER_ID = "order_id"

        fun createIntent(context: Context, orderId: String): Intent =
            Intent(context, PaymentActivity::class.java)
                .putExtra(EXTRA_ORDER_ID, orderId)
    }
}
