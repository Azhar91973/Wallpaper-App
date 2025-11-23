package com.example.dynamicwallpaper.Ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Common.NetworkResult
import com.example.dynamicwallpaper.MainActivity
import com.example.dynamicwallpaper.R
import com.example.dynamicwallpaper.Ui.auth.viewmodel.AuthViewModel
import com.example.dynamicwallpaper.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val authViewModel: AuthViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(inflater, container, false)
    }

    override fun setUpViews() {

    }

    override fun setUpClickListeners() {
        binding.buttonLogin.setOnClickListener {
            if (validateFields()) {
                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                val email = binding.editTextEmail.text.toString()
                val password = binding.editTextPassword.text.toString()
                authViewModel.login(email, password)
            }
        }
        binding.textSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun validateFields(): Boolean {
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()
        // Password Pattern to validate 
        val passwordPattern = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "Invalid email"
            return false
        } else if (password.isEmpty() || !passwordPattern.matches(password)) {
            binding.editTextPassword.error = "Invalid password"
            return false
        }
        return true
    }

    override fun setUpObservers() {
        authViewModel.loginSuccess.observe(viewLifecycleOwner) { loginSuccess ->
            when (loginSuccess) {
                is NetworkResult.Success -> {


                    Toast.makeText(requireContext(), "Login Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }

                is NetworkResult.Error -> {
                    Toast.makeText(
                        requireContext(), "Error ${loginSuccess.message}", Toast.LENGTH_SHORT
                    ).show()
                }

                is NetworkResult.Loading -> {
                    Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}