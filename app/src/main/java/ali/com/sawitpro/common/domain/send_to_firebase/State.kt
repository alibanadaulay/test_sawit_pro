package ali.com.sawitpro.common.domain.send_to_firebase

sealed class StateSendToFirebase {
    data class OnSuccess(val captureImage: CaptureImage) : StateSendToFirebase()
    data class OnError(val message: String) : StateSendToFirebase()
}
