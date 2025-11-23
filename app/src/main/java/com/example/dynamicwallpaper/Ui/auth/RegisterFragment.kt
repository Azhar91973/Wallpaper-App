package com.example.dynamicwallpaper.Ui.auth

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dynamicwallpaper.Common.BaseFragment
import com.example.dynamicwallpaper.Common.NetworkResult
import com.example.dynamicwallpaper.R
import com.example.dynamicwallpaper.Ui.auth.viewmodel.AuthViewModel
import com.example.dynamicwallpaper.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : BaseFragment<FragmentRegisterBinding>() {

    private val authVieModel: AuthViewModel by viewModels()
    override fun inflateBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentRegisterBinding {
        return FragmentRegisterBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        setUpClickListeners()
        setUpObservers()
    }

    override fun setUpViews() {

    }

    override fun setUpClickListeners() {
        binding.buttonSignup.setOnClickListener {
            if (validateFields()) {
                showToast("Validation Success")
                val email = binding.editTextEmail.text.toString()
                val password = binding.editTextPassword.text.toString()
                val name = binding.editTextName.text.toString()
                authVieModel.register(email, name, password)

            }
        }
    }

    private fun validateFields(): Boolean {
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()
        val cnfPassword = binding.editTextCnfPassword.text.toString()
        // Password Pattern to validate
        val passwordPattern = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "Invalid email"
            return false
        } else if (password.isEmpty() || !passwordPattern.matches(password)) {
            binding.editTextPassword.error = "Invalid password"
            return false
        } else if (cnfPassword.isEmpty() || !passwordPattern.matches(cnfPassword)) {
            binding.editTextCnfPassword.error = "Invalid password"
            return false
        } else if (password != cnfPassword) {
            binding.editTextCnfPassword.error = "Password does not match"
            return false
        }
        return true
    }

    override fun setUpObservers() {
        authVieModel.registerSuccess.observe(viewLifecycleOwner) { registerState ->
            when (registerState) {
                is NetworkResult.Success -> {
                    showToast("Registration Success")
                    Log.d("RegisterFragment", "setUpObservers: ${registerState.data}")
                    findNavController().navigateUp()
                }

                is NetworkResult.Error -> {
                    showToast("Registration Failed")
                    Log.d("RegisterFragment", "setUpObservers: ${registerState.message}")
                }

                is NetworkResult.Loading -> {
                    showToast("Loading")
                    Log.d("RegisterFragment", "setUpObservers: Loading ")
                }
            }

        }
    }

}