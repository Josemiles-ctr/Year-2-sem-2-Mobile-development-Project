package com.example.mobiledev.feature.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobiledev.data.repository.StaffRepository

class StaffViewModelFactory(private val repository: StaffRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StaffViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StaffViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
