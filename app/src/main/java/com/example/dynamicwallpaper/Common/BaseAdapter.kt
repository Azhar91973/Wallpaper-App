package com.example.dynamicwallpaper.Common

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

open class BaseAdapter<T> : RecyclerView.Adapter<BaseViewHolder<T>>() {
    var listOfItems: MutableList<T> = mutableListOf()

    open var expressionViewHolderBinding: ((T, ViewBinding) -> Unit)? = null
    open var expressionOnCreateViewHolder: ((ViewGroup) -> ViewBinding)? = null
    open var areItemsTheSame: ((T, T) -> Boolean)? = null
    open var areContentsTheSame: ((T, T) -> Boolean)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        return expressionOnCreateViewHolder?.let {
            it(parent)
        }?.let {
            BaseViewHolder(it, expressionViewHolderBinding!!)
        }!!

    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        holder.bind(listOfItems[position])
    }

    override fun getItemCount(): Int {
        return listOfItems.size
    }

    fun updateItems(newItems: List<T>) {
        if (areItemsTheSame != null && areContentsTheSame != null) {
            val diffResult = DiffUtil.calculateDiff(
                BaseDiffUtilCallback(listOfItems, newItems, areItemsTheSame!!, areContentsTheSame!!)
            )
            listOfItems.clear()
            listOfItems.addAll(newItems)
            diffResult.dispatchUpdatesTo(this)
        } else {
            listOfItems.clear()
            listOfItems.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    fun addItem(newItem: T) {
        listOfItems.add(newItem)
        notifyItemInserted(listOfItems.size - 1)
    }
}
class BaseViewHolder<T> internal constructor(
    private val binding: ViewBinding,
    private val expression: (T, ViewBinding) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: T) {
        expression(item, binding)
    }
}
