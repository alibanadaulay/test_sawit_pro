package ali.com.sawitpro.common.domain.send_to_firebase

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

interface SendToFirebase {

    operator fun invoke(bitmap: Bitmap?):Flow<StateSendToFirebase>
}