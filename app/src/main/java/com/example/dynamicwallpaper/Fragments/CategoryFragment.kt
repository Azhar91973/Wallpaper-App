package com.example.dynamicwallpaper.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.dynamicwallpaper.Common.BaseAdapter
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Models.CategoryItems
import com.example.dynamicwallpaper.R
import com.example.dynamicwallpaper.databinding.CategoryItemBinding
import com.example.dynamicwallpaper.databinding.FragmentCategoryBinding

class CategoryFragment : BaseFragment<FragmentCategoryBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentCategoryBinding {
        return FragmentCategoryBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun setUpViews() {
        setUpCategoryItems()
    }

    private fun setUpCategoryItems() {
        val items = listOf(
            CategoryItems(
                "https://images.pexels.com/photos/1283208/pexels-photo-1283208.jpeg", "Abstract"
            ), CategoryItems(
                "https://images.pexels.com/photos/1193743/pexels-photo-1193743.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Art"
            ), CategoryItems(
                "https://images.pexels.com/photos/4006534/pexels-photo-4006534.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Beach"
            ), CategoryItems(
                "https://images.pexels.com/photos/100582/pexels-photo-100582.jpeg", "Bicycle"
            ), CategoryItems(
                "https://images.unsplash.com/photo-1676631284463-cb0683105830?q=80&w=1887&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                "Bike"
            ), CategoryItems(
                "https://images.pexels.com/photos/707046/pexels-photo-707046.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Car"
            ), CategoryItems(
                "https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Food"
            ), CategoryItems(
                "https://images.pexels.com/photos/3165335/pexels-photo-3165335.jpeg", "Gaming"
            ), CategoryItems(
                "https://images.pexels.com/photos/36029/aroni-arsa-children-little.jpg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Girl"
            ), CategoryItems(
                "https://images.pexels.com/photos/216798/pexels-photo-216798.jpeg", "Nature"
            ), CategoryItems(
                "https://images.pexels.com/photos/1540406/pexels-photo-1540406.jpeg", "Music"
            ), CategoryItems(
                "https://images.pexels.com/photos/46148/aircraft-jet-landing-cloud-46148.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Plane"
            ), CategoryItems(
                "https://images.pexels.com/photos/1108572/pexels-photo-1108572.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Plant"
            ), CategoryItems(
                "https://images.pexels.com/photos/1202887/pexels-photo-1202887.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Rain"
            ), CategoryItems(
                "https://images.pexels.com/photos/41953/earth-blue-planet-globe-planet-41953.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Space"
            ), CategoryItems(
                "https://images.pexels.com/photos/335393/pexels-photo-335393.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Travel"
            ), CategoryItems(
                "https://images.pexels.com/photos/33045/lion-wild-africa-african.jpg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
                "Wildlife"
            ), CategoryItems(
                "https://images.pexels.com/photos/333850/pexels-photo-333850.jpeg?auto=compress&cs=tinysrgb",
                "Dark"
            )
        )

        val cAdapter = BaseAdapter<CategoryItems>()
        cAdapter.listOfItems = items.toMutableList()
        cAdapter.expressionOnCreateViewHolder = {
            CategoryItemBinding.inflate(layoutInflater, it, false)
        }
        cAdapter.expressionViewHolderBinding = { item, viewBinding ->
            val view = viewBinding as CategoryItemBinding
            Glide.with(requireContext()).load(item.categoryImgUrl).into(view.imgCategory)
            view.tvCategory.text = item.categoryName
            view.imgCategory.setOnClickListener {
                findNavController().navigate(
                    R.id.action_categoryFragment_to_searchFragment,
                    bundleOf("query" to item.categoryName, "flag" to true)
                )
            }
        }
        binding.rvCategoryItems.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvCategoryItems.adapter = cAdapter
    }

    override fun setUpClickListeners() {

    }

    override fun setUpObservers() {}
}