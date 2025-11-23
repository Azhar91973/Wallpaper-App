package com.example.dynamicwallpaper.Common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dynamicwallpaper.R

class CustomLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<CustomLoadStateAdapter.LoadStateViewHolder>() {

    class LoadStateViewHolder(view: View, retry: () -> Unit) : RecyclerView.ViewHolder(view) {
        private val retryButton: Button = view.findViewById(R.id.retry_button)
        private val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        private val errorMsg: TextView = view.findViewById(R.id.error_msg)

        init {
            retryButton.setOnClickListener { retry() }
        }

        fun bind(loadState: LoadState) {
            progressBar.visibility = if (loadState is LoadState.Loading) View.VISIBLE else View.GONE
            errorMsg.visibility = if (loadState is LoadState.Error) View.VISIBLE else View.GONE
            retryButton.visibility = if (loadState is LoadState.Error) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.load_state_footer, parent, false)
        return LoadStateViewHolder(view, retry)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    // New method to bind the load state to an external view
    fun bindToView(view: View, loadState: LoadState) {
        val viewHolder = LoadStateViewHolder(view, retry)
        viewHolder.bind(loadState)
    }
}
