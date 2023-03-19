package ali.com.sawitpro.show

import ali.com.sawitpro.common.domain.send_to_firebase.CaptureImage
import ali.com.sawitpro.databinding.ActivityShowBinding
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ShowActivity : AppCompatActivity() {
    companion object {

        const val captureImage: String = "PARAM_IMAGE"
    }

    private lateinit var _binding: ActivityShowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityShowBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getParcelable(captureImage, CaptureImage::class.java)
        } else {
            intent.extras?.getParcelable(captureImage) as CaptureImage?
        }
        with(_binding) {
            result?.let {
                tvDistance.setText(it.distance.toString())
                tvTime.setText(it.time.toString())
                tvText.setText(it.text)
            }
        }

    }
}