package com.example.myapplication.base.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T, V : ViewBinding> :
    RecyclerView.Adapter<BaseAdapter<T, V>.BaseViewHolder>() {

    val dataSet = ArrayList<T>()

    var onClick: ((T) -> Unit)? = null
    var onClickPosition: ((T, Int) -> Unit)? = null

    private var mRecyclerView: RecyclerView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(provideViewBinding(parent))
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        binData(holder.item, dataSet[position], position)
    }

    open fun setData(listItem: List<T>) {
        dataSet.clear()
        dataSet.addAll(listItem)
        notifyDataSetChanged()
    }

    fun getListData(): ArrayList<T> {
        return dataSet
    }

    open fun addMoreItem(listItem: ArrayList<T>) {
        val from = dataSet.size
        dataSet.addAll(listItem)
        notifyItemRangeInserted(from, listItem.size)
    }

    open fun addData(item: T) {
        dataSet.add(item)
        notifyItemInserted(dataSet.size - 1)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    open fun binData(viewBinding: V, item: T, position: Int) {}
    abstract fun provideViewBinding(parent: ViewGroup): V

    inner class BaseViewHolder(val item: V) : RecyclerView.ViewHolder(item.root)
}