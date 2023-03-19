package ali.com.sawitpro.common.domain.send_to_firebase

import ali.com.sawitpro.BuildConfig
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.database.FirebaseDatabase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL
import java.util.*

class SendToFirebaseImpl(
    private val recognizer: TextRecognizer,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val firebaseDatabase: FirebaseDatabase
) : SendToFirebase {

    companion object {

        private const val DESTINATION_LAT = "-6.1938383630962015"
        private const val DESTINATION_LNG = "106.821955468517835"
        private const val MODE = "driving"
        private const val TAG = "SendToFirebaseImpl"
    }

    override fun invoke(bitmap: Bitmap?): Flow<StateSendToFirebase> = flow {
        val text = getTextFromImage(bitmap)
        val url = getDistanceAndTime()
        getJson(url).let {
            if (it.isNotEmpty()) {
                val jsonObject = JSONObject(it)
                val jsonRoutes = jsonObject.getJSONArray("routes").optJSONObject(0)
                val jsonArrayLegs = jsonRoutes.getJSONArray("legs").optJSONObject(0)
                val distance = jsonArrayLegs.getJSONObject("distance").getInt("value")
                val time = jsonArrayLegs.getJSONObject("duration").getInt("value")
                val captureImage = CaptureImage(
                    text = text,
                    distance = distance,
                    time = time
                )
                if (sendModelToFirebase(captureImage)) {
                    emit(StateSendToFirebase.OnSuccess(captureImage = captureImage))
                }
            }
        }
    }
        .flowOn(Dispatchers.IO)

    private fun getJson(url: String): String {
        var output = ""
        try {
            val apiEnd = URL(url)
            val responseCode: Int
            val `is`: InputStream
            val connection: HttpURLConnection = apiEnd.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.readTimeout = 15000
            connection.connectTimeout = 15000
            connection.connect()
            responseCode = connection.responseCode
            `is` = if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            output = convertISToString(`is`)
            `is`.close()
            connection.disconnect()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: ProtocolException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return output
    }

    private suspend fun sendModelToFirebase(captureImage: CaptureImage): Boolean {
        var result = false
        firebaseDatabase.reference.child(captureImage.id).setValue(captureImage)
            .addOnCompleteListener {
                result = it.isSuccessful
                Log.e(TAG, "${it.isSuccessful}")
            }
            .addOnFailureListener {
                Log.e(TAG, it.message ?: "null")
            }
            .await()
        return result
    }

    private fun convertISToString(`is`: InputStream): String {
        val buffer = StringBuffer()
        try {
            var row: String?
            val br: BufferedReader = BufferedReader(InputStreamReader(`is`))
            while (br.readLine().also { row = it } != null) {
                buffer.append(row)
            }
            br.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return buffer.toString()
    }

    private suspend fun getTextFromImage(bitmap: Bitmap?): String {
        if (bitmap == null) {
            throw NullPointerException()
        }
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image)
        return result.await().text
    }
    @SuppressLint("MissingPermission")
    private suspend fun getDistanceAndTime(): String {
        val location = fusedLocationClient.lastLocation.await()
        val origin = "origin=${location.latitude},${location.longitude}"
        val destination = "destination=$DESTINATION_LAT,$DESTINATION_LNG"
        return "https://maps.googleapis.com/maps/api/directions/json?$origin&$destination&sensor=false&mode=$MODE&key=${BuildConfig.KEY}"
    }
}