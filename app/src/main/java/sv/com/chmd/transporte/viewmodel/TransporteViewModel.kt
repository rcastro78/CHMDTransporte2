package sv.com.chmd.transporte.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TransporteViewModel : ViewModel() {
    private val _isSlowNetwork = MutableStateFlow(false)
    val isSlowNetwork: StateFlow<Boolean> = _isSlowNetwork

    fun setSlowNetwork(value: Boolean) {
        _isSlowNetwork.value = value
    }
}