package ali.com.sawitpro.main

import ali.com.sawitpro.common.domain.send_to_firebase.SendToFirebase
import ali.com.sawitpro.common.domain.send_to_firebase.StateSendToFirebase
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainVM(private val sendToFirebase: SendToFirebase) : ViewModel() {

    private val _mainState: MutableLiveData<MainState> = MutableLiveData()
    val mainState = _mainState

    fun sendTextToFirebase(bitmap: Bitmap?) {
        viewModelScope.launch {
            sendToFirebase.invoke(bitmap)
                .collectLatest {
                    when (it) {
                        is StateSendToFirebase.OnError -> {

                        }
                        is StateSendToFirebase.OnSuccess -> {
                            _mainState.postValue(MainState.OnSuccess(it.captureImage))
                        }
                    }
                }
        }
    }

    fun stateIdle() {
        _mainState.value = MainState.Idle
    }
}