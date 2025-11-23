package com.example.dynamicwallpaper.Ui.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.impl.constraints.trackers.NetworkStateTracker
import com.example.dynamicwallpaper.Common.NetworkResult
import com.example.dynamicwallpaper.Ui.auth.model.UserResponse
import com.example.dynamicwallpaper.Ui.auth.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginSuccess = MutableLiveData<NetworkResult<UserResponse>>()
    val loginSuccess: LiveData<NetworkResult<UserResponse>> = _loginSuccess

    private val _registerSuccess = MutableLiveData<NetworkResult<UserResponse>>()

    val registerSuccess: LiveData<NetworkResult<UserResponse>> = _registerSuccess

    fun login(email: String, password: String) {
        _loginSuccess.value = NetworkResult.Loading
        viewModelScope.launch {
            _loginSuccess.value = authRepository.login(email, password)
        }
    }

    fun register(email: String, name: String, password: String) {
        _registerSuccess.value = NetworkResult.Loading
        viewModelScope.launch {
            _registerSuccess.value = authRepository.register(email, name, password)
        }
    }

}