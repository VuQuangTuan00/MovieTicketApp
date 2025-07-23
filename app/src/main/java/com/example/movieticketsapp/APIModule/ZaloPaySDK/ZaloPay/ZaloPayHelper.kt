package com.example.movieticketsapp.APIModule.ZaloPaySDK.ZaloPay

import android.app.Activity
import android.widget.Toast

import com.example.movieticketsapp.APIModule.ZaloPaySDK.Api.CreateOrder
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener

// ZaloPayHelper.kt
class ZaloPayHelper(private val activity: Activity) {

    fun pay(priceText: String, onSuccess: () -> Unit, onFailure: ((Exception?) -> Unit)? = null) {
        if (priceText.isEmpty()) {
            Toast.makeText(activity, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val orderApi =
            CreateOrder()

        try {
            val data = orderApi.createOrder(priceText)
            if (data.has("return_code") && data.getString("return_code") == "1") {
                val token = data.getString("zp_trans_token")

                ZaloPaySDK.getInstance().payOrder(activity, token, "demozpdk://app",
                    object : PayOrderListener {
                        override fun onPaymentSucceeded(transactionId: String?, transToken: String?, appTransID: String?) {
                            onSuccess()
                        }

                        override fun onPaymentCanceled(zpTransToken: String?, appTransID: String?) {
                            Toast.makeText(activity, "Payment Cancelled", Toast.LENGTH_SHORT).show()
                        }

                        override fun onPaymentError(zaloPayError: ZaloPayError?, zpTransToken: String?, appTransID: String?) {
                            Toast.makeText(activity, "Payment Failed: $zaloPayError", Toast.LENGTH_LONG).show()
                            onFailure?.invoke(Exception(zaloPayError?.toString()))
                        }
                    })

            } else {
                Toast.makeText(activity, "Unable to create order", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            onFailure?.invoke(e)
        }
    }
}
