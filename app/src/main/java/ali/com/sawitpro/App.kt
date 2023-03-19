package ali.com.sawitpro

import ali.com.sawitpro.common.domain.send_to_firebase.SendToFirebase
import ali.com.sawitpro.common.domain.send_to_firebase.SendToFirebaseImpl
import ali.com.sawitpro.main.MainVM
import android.app.Application
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            val appModule = module {
                single { LocationServices.getFusedLocationProviderClient(this@App) }
                single { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
                single { Firebase.database }
                single<SendToFirebase> { SendToFirebaseImpl(get(), get(), get()) }
                viewModelOf<MainVM> { MainVM(get()) }
            }
            modules(appModule)
        }
    }
}