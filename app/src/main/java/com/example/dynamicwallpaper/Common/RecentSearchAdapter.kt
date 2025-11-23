package com.example.dynamicwallpaper.Common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.dynamicwallpaper.R

class RecentSearchAdapter(
    context: Context,
    private val recentSearches: List<String>,
    private val removeRecentSearch: (String) -> Unit,
    private val setSearchQuery: (String) -> Unit
) : ArrayAdapter<String>(context, R.layout.searched_item, recentSearches) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.searched_item, parent, false
        )

        // Access and customize the TextView in searched_item.xml
        val textView = itemView.findViewById<TextView>(R.id.tv_searched_item)
        val imgRemove = itemView.findViewById<ImageView>(R.id.img_remove)
        textView.text = recentSearches[position]
        // Optional: Set click listeners or other logic
        textView.setOnClickListener {
            setSearchQuery(recentSearches[position])
        }
        imgRemove.setOnClickListener {
            removeRecentSearch(recentSearches[position])
        }

        return itemView
    }
}
