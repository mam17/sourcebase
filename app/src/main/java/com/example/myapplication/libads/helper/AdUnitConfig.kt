package com.example.myapplication.libads.helper

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AdUnitConfig(
    @SerializedName("enabled")
    @Expose var enabled: Boolean = true,
    @SerializedName("id")
    @Expose var id: String = ""
)
