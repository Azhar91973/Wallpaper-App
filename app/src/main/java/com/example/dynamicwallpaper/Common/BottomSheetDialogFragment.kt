package com.example.dynamicwallpaper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.dynamicwallpaper.WallpaperService.WallpaperHelper
import com.example.dynamicwallpaper.databinding.FragmentBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MyBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetDialogBinding? = null
    private val binding get() = _binding!!
    private var imageUrl: String? = null

    companion object {
        private const val IMAGE_URL_KEY = "image_url"
        fun newInstance(imageUrl: String): MyBottomSheetFragment {
            val fragment = MyBottomSheetFragment()
            val bundle = Bundle()
            bundle.putString(IMAGE_URL_KEY, imageUrl)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageUrl = arguments?.getString(IMAGE_URL_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBottomSheetDialogBinding.inflate(inflater, container, false)

        binding.homeScreenOption.setOnClickListener {
            imageUrl?.let { url ->
                context?.let { it1 ->
                    WallpaperHelper(it1).setWallpaper(
                        url, 0
                    )
                }
            } ?: showError()
            if (isAdded) dismiss()
        }

        binding.lockScreenOption.setOnClickListener {
            imageUrl?.let { url ->
                context?.let { it1 ->
                    WallpaperHelper(it1).setWallpaper(
                        url, 1
                    )
                }
            } ?: showError()
            if (isAdded) dismiss()
        }

        binding.bothOption.setOnClickListener {
            imageUrl?.let { url ->
                context?.let { it1 ->
                    WallpaperHelper(it1).setWallpaper(
                        url, 2
                    )
                }
            } ?: showError()
            if (isAdded) dismiss()
        }

        return binding.root
    }

    private fun showError() {
        Toast.makeText(requireContext(), "Image URL is invalid.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
