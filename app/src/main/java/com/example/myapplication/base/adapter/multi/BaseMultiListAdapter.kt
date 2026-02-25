package com.example.myapplication.base.adapter.multi

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.myapplication.base.adapter.BaseItemCallback
import com.example.myapplication.base.adapter.interfaces.MultiViewItem

abstract class BaseMultiListAdapter<T : MultiViewItem, V : ViewBinding>(
    diffCallback: BaseItemCallback<T>
) : ListAdapter<T, BaseMultiListAdapter<T, V>.BaseViewHolder>(diffCallback) {

    var onItemClick: ((T) -> Unit)? = null
    var onItemClickPosition: ((T, Int) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return getItem(position).getViewType()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(createBinding(parent, viewType))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        bindData(holder.binding, getItem(position), position)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(getItem(position))
            onItemClickPosition?.invoke(getItem(position), position)
        }
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            bindPayload(holder.binding, getItem(position), payloads)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    open fun bindPayload(binding: V, item: T, payloads: List<Any>) {}

    abstract fun createBinding(parent: ViewGroup, viewType: Int): V

    open fun bindData(binding: V, item: T, position: Int) {}

    inner class BaseViewHolder(val binding: V) :
        RecyclerView.ViewHolder(binding.root)
}