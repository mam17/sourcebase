package com.example.myapplication.base.adapter

import androidx.recyclerview.widget.DiffUtil

abstract class BaseItemCallback<T> : DiffUtil.ItemCallback<T>() {

    override fun getChangePayload(oldItem: T & Any, newItem: T & Any): Any? {
        return getPayload(oldItem, newItem)
    }

    open fun getPayload(oldItem: T, newItem: T): Any? = null
}