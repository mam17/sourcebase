package com.example.myapplication.base.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.myapplication.base.adapter.interfaces.DiffComparable

class BaseDiffUtil<T : DiffComparable<T>> :
    DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.areItemsTheSame(newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.areContentsTheSame(newItem)
    }
}