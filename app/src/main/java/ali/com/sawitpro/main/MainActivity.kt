package ali.com.sawitpro.main

import ali.com.sawitpro.databinding.ActivityMainBinding
import ali.com.sawitpro.show.ShowActivity
import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    companion object {

        private const val TAG = "MainActivity"
        private const val USER = "daulay.alibana@gmail.com"
        private const val PASS = "09090909090Abc"
        private const val REQUEST_CODE = 200
        private val PERMISSIONSS = arrayOf(
            CAMERA,
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION
        )
    }

    private lateinit var mainBinding: ActivityMainBinding
    private val _mainVM: MainVM by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        authUser()
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        with(mainBinding) {
            camera.setLifecycleOwner(this@MainActivity)
        }
        setListener()
        _mainVM.mainState.observe(this) {
            when (it) {
                MainState.Idle -> {// do nothing
                }
                is MainState.OnSuccess -> {
                    val intent = Intent(this@MainActivity, ShowActivity::class.java)
                    intent.putExtra(ShowActivity.captureImage, it.captureImage)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        _mainVM.stateIdle()
    }

    private fun authUser() {
        val auth = Firebase.auth
        auth.signInWithEmailAndPassword(USER, PASS)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun setListener() {
        mainBinding.btnTakePicture.setOnClickListener {
            if (checkPermission()) {
                mainBinding.camera.takePicture()
            }
        }
        mainBinding.camera.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                result.toBitmap {
                    _mainVM.sendTextToFirebase(it)
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ((grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
        ) {
            mainBinding.camera.takePicture()
        } else {
            finish()
        }
        return
    }

    private fun checkPermission(): Boolean {
        var result = true
        for (i in PERMISSIONSS.indices) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    PERMISSIONSS[i]
                ) == PackageManager.PERMISSION_DENIED
            ) {
                result = false
                requestPermission()
                break
            }
        }
        return result
    }

    private fun requestPermission() {
        requestPermissions(
            PERMISSIONSS,
            REQUEST_CODE
        )
    }
}