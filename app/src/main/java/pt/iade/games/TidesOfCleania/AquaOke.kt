package pt.iade.games.tidesofcleania

import android.Manifest
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Notes displayed on screen (ignore the octave)
    val noteBoxes = listOf("B", "A", "G", "F", "E", "D", "C")
    val boxHeight = 100.dp

    // Detect if we have a valid pitch
    val hasPitch = pitch > 0f

    // Get note name from detected pitch
    val noteName = remember(pitch) {
        if (hasPitch) frequencyToNoteAllOctaves(pitch)
        else "Invalid"
    }
    val baseNote = noteName.takeWhile { it.isLetter() }

    // Find matching box index
    val noteIndex = if (hasPitch)
        noteBoxes.indexOfFirst { it == baseNote }.takeIf { it >= 0 } ?: 0
    else
        -1

    // Animate vertical position of the pitch line
    val animatedOffset by animateDpAsState(
        targetValue = (noteIndex * boxHeight.value + boxHeight.value / 2).dp,
        label = "pitchLineOffset"
    )

    // Moving notes state
    var movingNotes by remember { mutableStateOf(listOf<MovingNote>()) }

    // Score
    var score by remember { mutableStateOf(0) }

    // Update notes position and spawn new notes
    LaunchedEffect(Unit) {
        while (true) {
            val deltaTime = 16L // ~60 FPS
            val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()

            movingNotes = movingNotes.map { note ->
                note.copy(x = note.x - note.speed * deltaTime / 1000f)
            }.filter { it.x + 50f > 0f } // 50f = note width, remove if off scree

            // Spawn new notes every 300px apart
            if (movingNotes.isEmpty() || movingNotes.last().x < screenWidth - 300f) {
                movingNotes = movingNotes + generateNote(screenWidth)
            }

            kotlinx.coroutines.delay(deltaTime)
        }
    }

    val density = LocalDensity.current
    val hitThreshold = with(density) { 20.dp.toPx() }
    val animatedOffsetPx = with(density) { animatedOffset.toPx() } // convert pitch line to px
    val boxHeightPx = with(density) { boxHeight.toPx() } // convert box height to px

    // Hit detection
    LaunchedEffect(movingNotes, pitch) {
        if (hasPitch) {
            val toRemove = movingNotes.filter { note ->
                val noteYPx = noteBoxes.indexOf(note.note) * boxHeightPx + boxHeightPx / 2
                abs(animatedOffsetPx - noteYPx) < hitThreshold && note.note == baseNote
            }
            if (toRemove.isNotEmpty()) {
                score += toRemove.size
                movingNotes = movingNotes - toRemove
            }
        }
    }

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
            // Draw note boxes
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

            // Draw moving notes
            movingNotes.forEach { note ->
                val noteY = noteBoxes.indexOf(note.note) * boxHeight.value
                Box(
                    modifier = Modifier
                        .offset(x = note.x.dp, y = noteY.dp)
                        .size(50.dp, boxHeight)
                        .background(Color.White)
                ) {
                    Text(
                        note.note,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                }
            }

            // Draw pitch line
            if (hasPitch) {
                Box(
                    modifier = Modifier
                        .offset(y = animatedOffset)
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color.Black)
                )
            }

            // Debug / Score display
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text("Score: $score", fontSize = 20.sp)
                Text("Pitch: %.2f Hz".format(pitch))
                Text("Note: $noteName")
            }
        }
    }
}

// Moving note class and generator
data class MovingNote(
    val note: String,
    var x: Float,
    val speed: Float = 200f
)

fun generateNote(screenWidth: Float): MovingNote {
    val notes = listOf("C", "D", "E", "F", "G", "A", "B")
    val note = notes.random()
    return MovingNote(note = note, x = screenWidth, speed = 200f)
}

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
