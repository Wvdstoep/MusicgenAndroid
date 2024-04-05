package com.goal.aicontent.navigation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NavigationViewModel : ViewModel() {
    val currentScreen = MutableLiveData<String>()

    fun setCurrentScreen(route: String) {
        currentScreen.value = route
    }
}
