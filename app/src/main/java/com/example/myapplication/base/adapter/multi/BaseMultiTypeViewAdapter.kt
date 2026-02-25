package com.example.myapplication.base.adapter.multi

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseMultiTypeViewAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val dataSet by lazy {
        initData()
    }
    var onClick: ((T) -> Unit)? = null

    abstract fun getViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

    abstract fun getViewType(position: Int): Int

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getItemViewType(position: Int): Int {
        return getViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return getViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        binData(holder, dataSet[position], position)
    }

    open fun setData(listItem: ArrayList<T>) {
        dataSet.clear()
        dataSet.addAll(listItem)
        notifyDataSetChanged()
    }

    fun getListData(): ArrayList<T> {
        return dataSet
    }

    open fun binData(viewHolder: RecyclerView.ViewHolder, item: T, position: Int){}

    open fun initData(): ArrayList<T> {
        return arrayListOf()
    }
}