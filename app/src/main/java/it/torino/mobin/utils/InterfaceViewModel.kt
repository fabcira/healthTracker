package it.torino.mobin.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InterfaceViewModel(applicationContext: Context) : ViewModel() {
    private val _showMarkers = MutableStateFlow(false)
    val showMarkers: StateFlow<Boolean> = _showMarkers

    fun setShowMarkers(show: Boolean) {
        _showMarkers.value = show
    }
    fun getShowMarkers(): MutableStateFlow<Boolean> {
        return _showMarkers
    }
}