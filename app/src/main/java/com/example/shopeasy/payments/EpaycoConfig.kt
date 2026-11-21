package com.example.shopeasy.payments

import com.example.shopeasy.BuildConfig

/**
 * Llaves de ePayco cargadas desde local.properties.
 * En producción la confirmación definitiva debe hacerse desde un backend.
 */
object EpaycoConfig {

    const val CHECKOUT_URL = "https://secure.epayco.co/checkout.php"
    const val RESPONSE_URL = "https://shopeasy.local/payment/result"
    const val CURRENCY = "COP"

    val customerId: String = BuildConfig.EPAYCO_P_CUST_ID_CLIENTE.trim()
    val pKey: String = BuildConfig.EPAYCO_P_KEY.trim()
    val publicKey: String = BuildConfig.EPAYCO_PUBLIC_KEY.trim()
    val confirmationUrl: String = BuildConfig.EPAYCO_CONFIRMATION_URL.trim()
    val isTest: Boolean = BuildConfig.EPAYCO_TEST

    val isConfigured: Boolean
        get() = customerId.isNotEmpty() && pKey.isNotEmpty()
}
