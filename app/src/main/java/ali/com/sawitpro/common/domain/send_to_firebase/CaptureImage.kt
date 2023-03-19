package ali.com.sawitpro.common.domain.send_to_firebase

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class CaptureImage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val distance: Int,
    val time: Int
) : Parcelable
