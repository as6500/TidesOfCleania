package pt.iade.games.tidesofcleania

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.httpGet
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlin.math.log2
import kotlin.math.roundToInt


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var pitch by remember { mutableFloatStateOf(0f) }
            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission Accepted: Do something
                    Log.d("HomeScreen","PERMISSION GRANTED")
//                    DetectPitchFromMic(
//                        context = context,
//                        activity = this,
//                        callback = { result, event ->
//                            pitch = result.pitch
//                            if (pitch > 0) { // valid pitch
//                                val note = frequencyToNoteAllOctaves(pitch)
//                                Log.i("Pitch", "Detected note: $note ($pitch Hz)")
//                            }
//                            //Log.i("Pitch", "Pitch is " + result.pitch + " Hz");
//                        })
                } else {
                    // Permission Denied: Do something
                    Log.d("HomeScreen","PERMISSION DENIED")
                }
            }

            "https://tidesofrubbish.onrender.com/getGameState".httpGet().response() {
                    request, response, result ->
                //Get JSON string from server response
                val jsonString = String(bytes = result.get())
                Log.i( "Test", jsonString)

                //Setup JSON and parse JSON
                val gson = GsonBuilder().create()
                val json = gson.fromJson<JsonObject>(jsonString, JsonObject().javaClass)

//                val sessionId = json.get("sessionId").asInt
//                val pairingCode = json.get("pairingCode").asString
//                val boostDuration = json.get("boostDuration").asInt

                //Log.i("Server", )
            }



            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        pitch,
                        onOpenAquaOke = {
                            val intent = Intent(this, AquaOke::class.java)
                            startActivity(intent)
                        }
                    )

                    SideEffect {
                        launcher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    pitch: Float,
    onOpenAquaOke: () -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 200.dp)
    ) {
//        Text(
//            text = "${frequencyToNoteAllOctaves(pitch)} ($pitch Hz)",
//            fontSize = 40.sp,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.fillMaxWidth()
//        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onOpenAquaOke) {
            Text("Start AquaOke")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(440f, {})
}

