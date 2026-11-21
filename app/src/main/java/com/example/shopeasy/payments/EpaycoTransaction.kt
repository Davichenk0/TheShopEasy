package com.example.shopeasy.payments

import com.example.shopeasy.data.PaymentDetails

data class EpaycoTransaction(
    val refPayco: String = "",
    val transactionId: String = "",
    val invoice: String = "",
    val amount: Double = 0.0,
    val currency: String = "",
    val response: String = "",
    val responseCode: Int = 0,
    val reason: String = "",
    val approvalCode: String = "",
    val franchise: String = "",
    val signatureValid: Boolean? = null
) {
    val isApproved: Boolean
        get() = responseCode == 1 || response.equals("Aceptada", ignoreCase = true)

    val isPending: Boolean
        get() = responseCode == 3 || response.equals("Pendiente", ignoreCase = true)

    fun toPaymentDetails() = PaymentDetails(
        refPayco = refPayco.ifBlank { null },
        transactionId = transactionId.ifBlank { null },
        response = response.ifBlank { reason.ifBlank { "Sin respuesta" } },
        approvalCode = approvalCode.ifBlank { null },
        franchise = franchise.ifBlank { null },
        signatureValid = signatureValid
    )
}
