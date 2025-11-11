package pt.iade.games.tidesofcleania

import android.Manifest
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.get
import kotlin.math.*

class AquaOke : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            var pitch by remember { mutableFloatStateOf(0f) }
            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission Accepted: Do something
                    Log.d("HomeScreen", "PERMISSION GRANTED")
                    DetectPitchFromMic(
                        context = context,
                        activity = this,
                        callback = { result, event ->
                            pitch = result.pitch
                            if (pitch > 0) { // valid pitch
                                val note = frequencyToNoteAllOctaves(pitch)
                                Log.i("Pitch", "Detected note: $note ($pitch Hz)")
                            }
                            //Log.i("Pitch", "Pitch is " + result.pitch + " Hz");
                        })
                } else {
                    // Permission Denied: Do something
                    Log.d("HomeScreen", "PERMISSION DENIED")
                }
            }


            MaterialTheme {
                AquaOkeScreen(440f, onBack = { finish() })
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AquaOkeScreen(
    pitch: Float,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

//    val soundPool = remember {
//        SoundPool.Builder()
//            .setMaxStreams(7)
//            .setAudioAttributes(
//                AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_GAME)
//                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                    .build()
//            )
//            .build()
//    }
//
//    val notes = remember { mutableStateListOf<Int>() }
//    var allLoaded by remember { mutableStateOf(false) }
//
//    LaunchedEffect(Unit) {
//        val rawIds = listOf(
//            R.raw.marimba_do,
//            R.raw.marimba_re,
//            R.raw.marimba_mi,
//            R.raw.marimba_fa,
//            R.raw.marimba_sol,
//            R.raw.marimba_la,
//            R.raw.marimba_si
//        )
//        var loadedCount = 0
//        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
//            if (status == 0) {
//                notes.add(sampleId)
//                loadedCount++
//                if (loadedCount == rawIds.size) {
//                    allLoaded = true
//                }
//            }
//        }
//        rawIds.forEach { soundPool.load(context, it, 1) }
//    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AquaOke") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
//                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

//        Column {
//            Text(
//                text = "${frequencyToNoteAllOctaves(pitch)} ($pitch Hz)",
//                fontSize = 40.sp,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column (
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xffe83b3b))
                ) {

                    Text(
                        text = "B",
                        fontSize = 40.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xfffb6b1d))
                ) {
                    Text(
                        text = "A",
                        fontSize = 40.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xfff79617) )
                ) {
                    Text(
                        text = "G",
                        fontSize = 40.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xff91db69) )
                ) {
                    Text(
                        text = "F",
                        fontSize = 40.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xff4d9be6))
                ) {
                    Text(
                        text = "E",
                        fontSize = 40.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xffa884f3))
                ) {
                    Text(
                        text = "D",
                        fontSize = 40.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xfff04f78))
                ) {
                    Text(
                        text = "C",
                        fontSize = 40.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

        }

        // Commenting this out to start making the actual layout
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            val noteLabels = listOf("Do", "Re", "Mi", "Fa", "Sol", "La", "Si")
//            noteLabels.forEachIndexed { index, label ->
//                Button(
//                    onClick = { playNote(index) },
//                    enabled = allLoaded, // disable until loaded
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                ) {
//                    Text(label)
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(
//                onClick = { playRandomSong() },
//                enabled = allLoaded
//            ) {
//                Text("Play Random Song")
//            }
//
//            if (!allLoaded) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Text("Loading sounds...")
//            }
//        }
    }
}

//fun playNote(
//    index: Int,
//    allLoaded: Boolean,
//    notes: List<Int>,
//    soundPool: SoundPool) {
//    if (allLoaded && index in notes.indices) {
//        soundPool.play(notes[index], 1f, 1f, 0, 0, 1f)
//    }
//}
//
//fun playRandomSong(coroutineScope: CoroutineScope) {
//    val randomNotes = List(20) { (0..6).random() }
//    coroutineScope.launch {
//        for ( i in randomNotes) {
//            playNote(i)
//            delay(600L)
//        }
//    }
//}

@Preview(showBackground = true)
@Composable
fun AquaOkeScreenPreview() {
    AquaOkeScreen(440f, {})
}
