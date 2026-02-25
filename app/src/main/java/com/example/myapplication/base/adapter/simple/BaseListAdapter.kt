package com.example.myapplication.base.adapter.simple

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseListAdapter<T, V : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, BaseListAdapter<T, V>.BaseViewHolder>(diffCallback) {

    var onClick: ((T) -> Unit)? = null
    var onClickPosition: ((T, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(provideViewBinding(parent))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        bindData(holder.binding, item, position)

        holder.itemView.setOnClickListener {
            onClick?.invoke(item)
            onClickPosition?.invoke(item, position)
        }
    }

    fun submitData(list: List<T>) {
        submitList(list)
    }

    open fun bindData(binding: V, item: T, position: Int) {}

    abstract fun provideViewBinding(parent: ViewGroup): V

    inner class BaseViewHolder(val binding: V) :
        RecyclerView.ViewHolder(binding.root)
}