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
import androidx.compose.animation.core.animateDpAsState
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
                AquaOkeScreen(pitch, onBack = { finish() })
                SideEffect {
                    launcher.launch(Manifest.permission.RECORD_AUDIO)
                }
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

    // Notes displayed on screen; ignore the octave
    val noteBoxes = listOf("B", "A", "G", "F", "E", "D", "C")
    val boxHeight = 100.dp

    // Detect if we have a valid pitch
    val hasPitch = pitch > 0f

    // Get note name from detected pitch
    val noteName = remember(pitch) {
        if (hasPitch) frequencyToNoteAllOctaves(pitch)
        else "Invalid"
    }
    // Extract just the letter (ignore octave and sharps)
    val baseNote = noteName.takeWhile { it.isLetter() }

    // Find matching box index
    val noteIndex = if (hasPitch)
        noteBoxes.indexOfFirst { it == baseNote }.takeIf { it >= 0 } ?: 0
    else
        -1 // The number to hide the line

    // Animate vertical position of the line
    val animatedOffset by animateDpAsState(
        targetValue = (noteIndex * boxHeight.value + boxHeight.value/2).dp,
        label = "pitchLineOffset"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AquaOke") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Make the note boxes in different colors
            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                val colors = listOf(
                    Color(0xffe83b3b),
                    Color(0xfffb6b1d),
                    Color(0xfff79617),
                    Color(0xff91db69),
                    Color(0xff4d9be6),
                    Color(0xffa884f3),
                    Color(0xfff04f78)
                )

                noteBoxes.forEachIndexed { index, note ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(boxHeight)
                            .background(colors[index])
                    ) {
                        Text(
                            text = note,
                            fontSize = 40.sp,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp)
                        )
                    }
                }
            }

            // Line of current pitch of player
            if (hasPitch) {
                Box(
                    modifier = Modifier
                        .offset(y = animatedOffset)
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.Black)
                )
            }

            // Debug values to display
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text("Pitch: %.2f Hz".format(pitch))
                Text("Note: $noteName")
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

fun frequencyToNoteAllOctaves(freq: Float): String {
    // Create an array of the 12 note names in one octave
    val notes = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    // Check if there is no sound to evaluate
    if (freq <= 0) return "Invalid frequency"
    // Get the number of semitones difference from A4(440 Hz)
    val numberOfSemitones = 12 * log2(freq / 440.0)
    // Rounding to get to the nearest semitone
    val semitone = numberOfSemitones.roundToInt()
    // Get the note from the semitone
    val noteIndex = (semitone + 9).mod(12)
    // Get the octave from the semitone
    val octave = 4 + ((semitone + 9) / 12)
    return "${notes[noteIndex]}$octave"
}