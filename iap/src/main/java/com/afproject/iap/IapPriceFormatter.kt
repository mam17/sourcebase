package com.afproject.iap

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object IapPriceFormatter {
    fun formatPerWeek(priceAmountMicros: Long, priceCurrencyCode: String, numWeek: Int): String {
        val price = (priceAmountMicros / 1_000_000.0) / numWeek
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            currency = Currency.getInstance(priceCurrencyCode)
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
        var formatted = format.format(price)
        if (price >= 10) {
            formatted = formatted.replace(Regex("([.,]\\d{1,2})(?!\\d)"), "")
        } else {
            formatted = formatted.replace(Regex("([.,]00)(?!\\d)"), "")
        }
        return formatted
    }
}
