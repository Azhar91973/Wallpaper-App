package com.example.dynamicwallpaper.Common

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * A generic base adapter for RecyclerView that uses ViewBinding.
 *
 * This adapter supports differential updates using DiffUtil and optional item selection handling.
 *
 * @param T The type of the data items.
 * @param VB The type of the ViewBinding for each item view.
 */
abstract class BaseAdapter<T : Any, VB : ViewBinding> :
    RecyclerView.Adapter<BaseViewHolder<T, VB>>() {

    /**
     * The list of items managed by the adapter.
     */
    protected val items = mutableListOf<T>()

    /**
     * A set containing the unique IDs of selected items.
     */
    private val selectedIds = mutableSetOf<String>()

    // --------------------------------------------------------------------------------------------
    // Abstract members to be implemented by concrete adapters
    // --------------------------------------------------------------------------------------------

    /**
     * Creates and returns an instance of the ViewBinding for an item view.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @return A new instance of the ViewBinding.
     */
    abstract fun createBinding(parent: ViewGroup): VB

    /**
     * Returns a unique identifier for the given item.
     *
     * @param item The item for which to obtain an ID.
     * @return A unique identifier as a String.
     */
    abstract fun getItemId(item: T): String

    /**
     * Binds the data item to the provided view binding.
     *
     * @param binding The ViewBinding for the item view.
     * @param item The data item to bind.
     * @param isSelected Indicates if the item is currently selected.
     */
    abstract fun bindView(binding: VB, item: T, isSelected: Boolean)

    /**
     * Checks whether two items represent the same entity.
     *
     * @param oldItem The old item.
     * @param newItem The new item.
     * @return True if both items represent the same entity.
     */
    abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

    /**
     * Checks whether the contents of two items are the same.
     *
     * @param oldItem The old item.
     * @param newItem The new item.
     * @return True if the contents of both items are identical.
     */
    abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean

    // --------------------------------------------------------------------------------------------
    // Click listener setup
    // --------------------------------------------------------------------------------------------

    /**
     * A callback invoked when an item is clicked.
     */
    var onItemClick: ((T) -> Unit)? = null

    /**
     * A callback invoked when an item is long clicked.
     */
    var onItemLongClick: ((T) -> Boolean)? = null

    // --------------------------------------------------------------------------------------------
    // RecyclerView.Adapter implementation
    // --------------------------------------------------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T, VB> {
        val binding = createBinding(parent)
        return BaseViewHolder(binding, this).apply {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(items[position])
                }
            }
            itemView.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick?.invoke(items[position]) ?: false
                } else false
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T, VB>, position: Int) {
        val item = items[position]
        val isSelected = selectedIds.contains(getItemId(item))
        holder.bind(item, isSelected)
    }

    override fun getItemCount() = items.size

    /**
     * Updates the adapter with a new list of items.
     *
     * This method calculates the differences between the old and new lists using DiffUtil,
     * and dispatches the updates accordingly.
     *
     * @param newItems The new list of items to display.
     */
    fun submitList(newItems: List<T>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size

            override fun getNewListSize() = newItems.size

            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                areItemsTheSame(items[oldPos], newItems[newPos])

            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                areContentsTheSame(items[oldPos], newItems[newPos])
        })

        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Toggles the selection state of the item with the specified ID.
     *
     * @param itemId The unique identifier of the item.
     */
    fun toggleSelection(itemId: String) {
        val position = items.indexOfFirst { getItemId(it) == itemId }
        if (position != RecyclerView.NO_POSITION) {
            if (selectedIds.contains(itemId)) {
                selectedIds.remove(itemId)
            } else {
                selectedIds.add(itemId)
            }
            notifyItemChanged(position)
        }
    }

    /**
     * Clears the selection state of all items.
     */
    fun clearSelections() {
        val changedPositions = items.indices.filter { index ->
            selectedIds.contains(getItemId(items[index]))
        }
        selectedIds.clear()
        changedPositions.forEach { notifyItemChanged(it) }
    }

    // --------------------------------------------------------------------------------------------
    // Helper functions for selection
    // --------------------------------------------------------------------------------------------

    /**
     * Checks if the specified item is selected.
     *
     * @param item The item to check.
     * @return True if the item is selected; false otherwise.
     */
    fun isSelected(item: T): Boolean = selectedIds.contains(getItemId(item))

    /**
     * Returns the total number of selected items.
     *
     * @return The count of selected items.
     */
    fun getSelectedCount(): Int = selectedIds.size

    /**
     * Retrieves a set of unique IDs for the currently selected items.
     *
     * @return A set containing the IDs of selected items.
     */
    fun getSelectedIds(): Set<String> = selectedIds.toSet()
}

/**
 * A generic ViewHolder for use with [BaseAdapter].
 *
 * @param T The type of the data items.
 * @param VB The type of the ViewBinding for the item view.
 *
 * @property binding The view binding associated with this ViewHolder.
 * @property adapter The adapter that created this ViewHolder.
 */
class BaseViewHolder<T : Any, VB : ViewBinding>(
    private val binding: VB,
    private val adapter: BaseAdapter<T, VB>,
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Binds the given data item to the view.
     *
     * @param item The data item to bind.
     * @param isSelected Indicates whether the item is selected.
     */
    fun bind(item: T, isSelected: Boolean) {
        adapter.bindView(binding, item, isSelected)
    }
}