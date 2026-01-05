package com.example.myapplication.libads.data

data class AdRevenueData(
    val valueMicros: Long,
    val currency: String = "USD",
    val adType: String,
    val placement: String,
    val mediation: String,
    val precision: Int
) {
    val revenue: Double get() = valueMicros / 1_000_000.0
}
