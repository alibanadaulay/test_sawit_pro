package ali.com.sawitpro.main

import ali.com.sawitpro.common.domain.send_to_firebase.CaptureImage

sealed class MainState {
    data class OnSuccess(val captureImage: CaptureImage):MainState()
    object Idle:MainState()
}
