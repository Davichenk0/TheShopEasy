package com.example.shopeasy.payments

import com.example.shopeasy.data.Order
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.TimeUnit

object EpaycoPaymentService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Cuerpo del formulario que se publica en el WebView hacia el
     * Standard Checkout de ePayco.
     */
    fun buildCheckoutBody(order: Order): String {
        require(EpaycoConfig.isConfigured) { "ePayco no está configurado." }

        val amount = formatAmount(order.total)
        val email = order.customerEmail.ifBlank { "cliente@shopeasy.com" }
        val signature = md5(
            "${EpaycoConfig.customerId}^${EpaycoConfig.pKey}^${order.id}^$amount^${EpaycoConfig.CURRENCY}"
        )

        val params = linkedMapOf(
            "p_cust_id_cliente" to EpaycoConfig.customerId,
            "p_key" to EpaycoConfig.pKey,
            "p_id_invoice" to order.id,
            "p_description" to "ShopEasy - Pedido ${order.id.take(8).uppercase()}",
            "p_amount" to amount,
            "p_amount_base" to amount,
            "p_base_tax" to amount,
            "p_tax" to "0.00",
            "p_email" to email,
            "p_billing_email" to email,
            "p_currency_code" to EpaycoConfig.CURRENCY,
            "p_signature" to signature,
            "p_test_request" to if (EpaycoConfig.isTest) "TRUE" else "FALSE",
            "p_url_response" to EpaycoConfig.RESPONSE_URL,
            "country" to "CO",
            "p_extra1" to order.id,
            "p_extra2" to email
        )

        if (EpaycoConfig.confirmationUrl.isNotBlank()) {
            params["p_url_confirmation"] = EpaycoConfig.confirmationUrl
            params["p_confirm_method"] = "POST"
        }

        return params.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }
    }

    /**
     * Consulta el estado real de la transacción en ePayco a partir de la
     * referencia devuelta en la URL de respuesta.
     */
    suspend fun validateTransaction(refPayco: String): EpaycoTransaction =
        withContext(Dispatchers.IO) {
            val url = "https://secure.epayco.co/validation/v1/reference/${encode(refPayco)}"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("ePayco respondió con código ${response.code}.")
                }
                val body = response.body?.string().orEmpty()
                parseTransaction(body)
            }
        }

    private fun parseTransaction(body: String): EpaycoTransaction {
        val root = JsonParser.parseString(body).asJsonObject
        val data = root.get("data")
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
            ?: root

        fun text(vararg keys: String): String = keys.firstNotNullOfOrNull { key ->
            data.get(key)?.takeIf { !it.isJsonNull }?.let { element ->
                if (element.isJsonPrimitive) element.asJsonPrimitive.asString
                else element.toString()
            }
        }.orEmpty()

        val amountRaw = text("x_amount", "x_amount_ok", "amount")
        val currencyRaw = text("x_currency_code", "currency")
        val transactionId = text("x_transaction_id", "x_transactionid")

        return EpaycoTransaction(
            refPayco = text("ref_payco", "x_ref_payco"),
            transactionId = transactionId,
            invoice = text("x_id_invoice", "x_id_factura"),
            amount = amountRaw.toDoubleOrNull() ?: 0.0,
            currency = currencyRaw.uppercase(Locale.ROOT),
            response = text("x_response"),
            responseCode = text("x_cod_response").toIntOrNull() ?: 0,
            reason = text("x_response_reason_text"),
            approvalCode = text("x_approval_code"),
            franchise = text("x_franchise", "x_bank_name"),
            signatureValid = verifySignature(
                signature = text("x_signature"),
                refPayco = text("x_ref_payco", "ref_payco"),
                transactionId = transactionId,
                amountRaw = amountRaw,
                currency = currencyRaw
            )
        )
    }

    private fun verifySignature(
        signature: String,
        refPayco: String,
        transactionId: String,
        amountRaw: String,
        currency: String
    ): Boolean? {
        if (signature.isBlank() || !EpaycoConfig.isConfigured) return null

        val payload = listOf(
            EpaycoConfig.customerId,
            EpaycoConfig.pKey,
            refPayco,
            transactionId,
            amountRaw,
            currency
        ).joinToString("^")

        val expected = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

        return expected.equals(signature, ignoreCase = true)
    }

    private fun formatAmount(amount: Double): String =
        BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).toPlainString()

    private fun md5(value: String): String =
        MessageDigest.getInstance("MD5")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}
